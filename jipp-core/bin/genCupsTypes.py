#!/usr/bin/python3

# Copyright 2020 HP Development Company, L.P.
#
# * Reads registrations from https://www.cups.org/doc/spec-ipp.html
# * Converts them into Kotlin files
# * Emits warnings during conversions
#

import re, pprint, os, sys
from urllib.request import urlopen
from bs4 import BeautifulSoup # sudo apt install python-bs4
from jinja2 import Environment, FileSystemLoader # pip install Jinja2

pp = pprint.PrettyPrinter(indent=2, width=120)
proj_dir = os.path.dirname(os.path.realpath(__file__)) + "/../"
html = urlopen("https://www.cups.org/doc/spec-ipp.html")
cups = BeautifulSoup(html.read(), features="lxml")
out_files = [ ]
operations = { }
types = { }

def find_operations():
    # Read ops from the table
    table = next(t for t in cups.find_all('table') if t['summary'] == 'Supported Operations')
    base_operations = { }
    for r in table.find_all('tr'):
        cells = r.find_all('td')
        if len(cells) == 4 and 'deprecate' not in cells[3].get_text() and '0x4' in cells[2].get_text():
            operation = {
                'name': cells[0].get_text(),
                'version': cells[1].get_text(),
                'code': cells[2].get_text(),
                'description': cells[3].get_text(),
            }
            base_operations[operation['name']] = operation

    # Now look for the ones that matter
    for h3 in cups.find_all('h3', class_='title'):
        if "Operation" in h3.get_text() and "Deprecated" not in h3.get_text():
            operation = next((key for key in base_operations.keys() if key in h3.get_text()), None)
            if operation:
                operations[operation] = base_operations[operation]

def find_types():
    syntax_re = re.compile(r"""\(.+\)""")
    keyword_re = re.compile(r"""'(.*)'(: (.+))?""")

    for type in cups.find_all('h4'):
        if 'Deprecated' not in type.get_text():
            if type.a and syntax_re.search(type.a.contents[0]):
                text = type.a.contents[0]
            elif syntax_re.search(type.contents[0]):
                text = type.contents[0]
            else:
                text = None

            if text:
                found = syntax_re.search(text)
                name = text[0:found.start()].strip()
                syntax = found.group(0).lower()
                types[name] = {
                    'name': name,
                    'syntax': syntax
                }

                if 'keyword' in syntax:
                    # Look for the first ul afterwards
                    ul = type.next_element
                    while ul.name != 'ul' and ul.name != 'h4':
                        ul = ul.next_element
                    if ul.name == 'ul':
                        keywords = [ ]
                        for li in ul.find_all('li'):
                            m = keyword_re.search(li.contents[0])
                            keyword = {
                                'name': m.group(1)
                            }
                            if m.group(3):
                                keyword['description'] = m.group(3)
                            keywords.append(keyword)
                        types[name]['keywords'] = keywords

                if 'enum' in syntax:
                    table = type.next_element
                    while table.name != 'table' and table.name != 'h4':
                        table = table.next_element
                    if table.name == 'table':
                        enums = [ ]
                        for tr in table.find_all('tr'):
                            tds = tr.find_all('td')
                            if len(tds) > 1:
                                enums.append({'code': tds[0].contents[0], 'description': tds[1].contents[0]})
                        if enums:
                            types[name]['enums'] = enums

def fix_ktypes():
    name_re = re.compile(r"""name\(.*\)""")
    text_re = re.compile(r"""text\(.*\)""")
    int_re = re.compile(r"""integer(\(.*\))?""")
    keyword_re = re.compile(r"""type[23] keyword""")

    for type in types.values():
        if 'syntax' not in type:
            continue
        syntax = type['syntax']

        if '1setof ' in syntax:
            syntax = syntax.replace('1setof ', '')
            set = True
        else:
            set = False
        syntax = syntax.replace('| novalue', '')
        if syntax.endswith(')') and syntax.startswith('('):
            syntax = syntax[1:-1]
        if name_re.match(syntax):
            ktype = "NameType"
        elif text_re.match(syntax):
            ktype = "TextType"
        elif int_re.match(syntax):
            ktype = "IntType"
        elif keyword_re.match(syntax):
            ktype = 'KeywordType'
        elif syntax == 'uri':
            ktype = 'UriType'
        elif syntax == 'type2 enum':
            if type['name'] != 'printer-type' and type['name'] != 'printer-type-mask':
                raise Exception("Unexpected enum %s" % type['name'])
            ktype = 'BitfieldType'
        elif syntax == 'keyword | name(max)':
            ktype = 'KeywordOrNameType'
        else:
            raise Exception("Unrecognized type %s" % type)
        if set:
            ktype = "%s.Set" % ktype
        if not ktype:
            print("Fail on %s" % type)
        type['ktype'] = ktype

def prep_file(name, suffix='.kt'):
    out_file = os.path.abspath(proj_dir + 'src/main/java/com/hp/jipp/cups/' + camel_class(name) + suffix)
    if not os.path.exists(os.path.dirname(out_file)):
        os.makedirs(os.path.dirname(out_file))
    #print out_file
    if out_file in out_files:
        warn("About to replace " + out_file + ", two competing definitions?")
    out_files.append(out_file)
    return out_file

# Accepts any string, returning in the form CamelClass
def camel_class(string):
    parts = [word.lower().capitalize() for word in re.split("[ _-]", string) if len(word)]
    combined = ""
    for part in parts:
        part = part.replace('.', 'p')
        if combined and combined[-1].isdigit() and part[0].isdigit():
            combined += '_'
        combined += part
    return combined

# Prevent the string from starting with a numeric
def not_numeric(string):
    if string[0].isdigit():
        return "num" + string
    else:
        return string

# All current java keywords; must be avoided
java_keywords = [
    "abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized",
    "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte",
    "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch",
    "extends", "int", "short", "try", "char", "final", "interface", "static", "void", "class", "finally",
    "long", "strictfp", "volatile", "const", "float", "native", "super", "while"
]

# Return a safe version of the enclosed string (prefixed with _ if a java keyword)
def java_safe(string):
    if string in java_keywords:
        return "_" + string
    else:
        return string

# Accepts any string, returning in the form camelClass
def camel_member(string):
    value = camel_class(string)
    if len(value) > 1:
        return java_safe(not_numeric(value[0].lower() + value[1:]))
    else:
        return java_safe(not_numeric(value[0].lower()))

def rstrip_all(text):
    return re.sub(' +\n', '\n', text)

def emit_cups():
    env = Environment(loader=FileSystemLoader(proj_dir + 'bin'))
    env.filters['camel_class'] = camel_class
    env.filters['camel_member'] = camel_member
    template = env.get_template('cups.kt.tmpl')
    with open(prep_file("cups"), "w") as file:
        file.write(rstrip_all(template.render(
            operations=sorted(operations.values(), key=lambda o: o['code']),
            types=sorted(types.values(), key=lambda o: o['name']),
            app=os.path.basename(sys.argv[0]))))

find_operations()
find_types()

# Patch up some missing data
types['printer-type-mask']['enum_ref'] = 'printer-type'
types['notify-events'] = {
    'name': 'notify-events',
    'keywords': [
        { 'name': 'printer-added', 'description': 'Get notified whenever a printer or class is added' },
        { 'name': 'printer-deleted', 'description': 'Get notified whenever a printer or class is deleted' },
        { 'name': 'printer-modified', 'description': 'Get notified whenever a printer or class is modified' },
        { 'name': 'server-audit', 'description': 'Get notified when a security condition occurs' },
        { 'name': 'server-restarted', 'description': 'Get notified when the server is restarted' },
        { 'name': 'server-started', 'description': 'Get notified when the server is started' },
        { 'name': 'server-stopped', 'description': 'Get notified when the server is stopped' },
    ]}

# Already specified in IPP
del types['printer-dns-sd-name']
del types['job-cancel-after']
del types['job-hold-until']
del types['job-sheets']

fix_ktypes()
emit_cups()

#print(pp.pformat(operations))
#print(pp.pformat(types))
