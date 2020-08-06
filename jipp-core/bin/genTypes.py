#!/usr/bin/python3
#
# * Reads registrations from http://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml
# * Converts them into Kotlin files
# * Emits warnings during conversions
#

import urllib.request, urllib.parse, urllib.error
from lxml import etree
import copy
import re
import pprint
import os.path
import sys
from jinja2 import Environment, FileSystemLoader # pip install Jinja2

# Global data
specs = { }
enums = { }
key_values = { }
keywords = { }
attributes = { }
collections = { }
pending_collections = { }
out_files = [ ]
proj_dir = os.path.dirname(os.path.realpath(__file__)) + "/../"
warns = 0
pp = pprint.PrettyPrinter(indent=2)

# Some enums/keywords have a plural 's' on the end, which we remove for clarity.
# Types ending with these words are not plural for our purposes and should keep their s:
nonplurals = [ 'sides', 'status', 'print-supports', 'details', 'which-jobs' ]

# XML Fix: there's no tag for keyValue sadly. So we'll fake it, knowing which ones are better expressed this way.
key_value_type_names = [
    'document-metadata',
    'printer-alert',
    'printer-finisher',
    'printer-finisher-supplies',
    'printer-input-tray',
    'printer-output-tray',
    'printer-supply'
]

def pretty(o):
    return pp.pformat(o).replace('\n', '\n    ')

# Tell the user we didn't completely understand something
def warn(output, object = None):
    global warns
    warns = warns + 1
    print("WARN: " + output)
    if object is not None:
        print(('    ' + pp.pformat(object).replace('\n', '\n    ')))

def note(output):
    print("    NOTE: " + output)

# Given a record, attempt to grab the referenced specification out of its xref.
def parse_spec(xref, target):
    if xref is None:
        return None

    spec = None
    uri = None
    if xref.attrib['type'] == 'rfc':
        spec = xref.attrib['data']
        uri = "http://www.iana.org/go/" + spec

    if xref.attrib['type'] == 'uri':
        spec = xref.text
        uri = xref.attrib['data']

    if spec is None or uri is None:
        warn("unparseable spec reference " + etree.tostring(xref))
    else:
        if spec not in specs:
            specs[spec.lower()] = uri
            specs[spec.upper()] = uri

    if spec is not None and spec not in target['specs']:
        target['specs'].append(spec)

# XML Fix
set_attributes = [
    'number-up-supported'
]

def fix_syntax(item, syntax = None):
    if syntax is None:
        syntax = item['syntax']

    # XML fix (it's an enum, not a keyword)
    if 'name' in item and item['name'] == 'input-orientation-requested':
        syntax = 'enum'

    # XML fix
    syntax = syntax.replace("integer:0:MAX", "integer(0:MAX)")

    # XML fix
    syntax = re.sub("\]\s+\[.*\]", "", syntax)

    # XML fix: make ranges consistent
    syntax = re.sub(' *: *',':', syntax)

    # Some strings we do not care about no matter where they occur
    syntax = syntax.replace("(MAX)", "")
    syntax = syntax.replace("type1", "")
    syntax = syntax.replace("type2", "")
    syntax = re.sub('\[.*\]$','', syntax) # XML fix
    syntax = syntax.replace("1set Of", "1setOf") # XML fix
    if '1setOf' in syntax or item.get('name', None) in set_attributes:
        syntax = syntax.replace("1setOf", "")
        item['set'] = True
    syntax = syntax.strip()

    if syntax.startswith('(') and syntax.endswith(')'):
        return fix_syntax(item, syntax[1:-1])

    # XML fix to clean up "(name(MAX)" (missing term paren)
    if syntax.startswith('(') and not syntax.endswith(')'):
        return fix_syntax(item, syntax[1:])

    if " | " in syntax:
        # Ignore no-value and unknown since those are accepted everywhere
        parts = sorted([fix_syntax({}, part.strip()) for part in syntax.split("|")])
        syntax = " | ".join([part for part in parts if part != "no-value" and part != "unknown"])
    item['syntax'] = syntax
    return syntax

# Parse a single enum record into the global list of enums
def parse_enum(record):
    attribute = record.find('{*}attribute').text

    enum = enums.setdefault(attribute, { 'name': attribute, 'values': { }, 'specs': [ ],
                                         'syntax': record.find('{*}syntax').text })
    fix_syntax(enum)
    parse_spec(record.find('{*}xref'), enum)

    value = record.find('{*}value')
    if value is not None:
        value = value.text

    name = record.find('{*}name')
    if name is not None:
        name = name.text
        if name is not None:
            m = re.search("are the same.*\"([a-z-]+)\"", name)
            if m and m.group(1):
                value = "any " + m.group(1) + " enum value"
                name = None

    if enum['name'] == 'fetch-status-code':
        # fetch-status-code defined as <All "status-code" value other than 'successful-ok'>
        enum['ref'] = 'status'
        return

    if value is None:
        return

    if name is None:
        m = re.search("<? ?(?:[aA]ny |all )\"?([a-z-]+)\"?>?", value)
        if m and m.group(1):
            enum['ref'] = m.group(1)
        else:
            enum['bad'] = True
            warn("enum " + attribute + " has unparseable value '" + value + "'")
        return

    # Totally ignore (deprecated), Reserved, (Under Review), etc.
    if re.search("\(.*\)", name) or "Reserved" in name:
        return

    try:
        if value.startswith("0x"):
            enum['hex'] = True
        value = int(value, 0)
        enum['values'][name] = value
    except ValueError:
        warn("enum " + attribute + " has non-integer value " + value)

# Parse a single status code
def parse_status_code(record):
    enum = enums.setdefault('status', { 'name': 'status', 'values': { }, 'specs': [ ], 'hex': True })
    value = record.find('{*}value').text
    name = record.find('{*}name').text
    parse_spec(record.find('{*}xref'), enum)
    if name == "Unassigned" or name.startswith("Reserved") or '-' in value:
        return
    try:
        value = int(value, 0)
        enum['values'][name] = value
    except ValueError:
        warn("status code has non-integer value " + value)

# XML Fix
obsolete_keywords = [
    'job-cover-back-supported',
    'job-cover-front-supported',
]

# Parse a single keyword record
def parse_keyword(record):
    attribute = record.find('{*}attribute').text

    if attribute in obsolete_keywords:
        return

    # XML Fix: proof-print-supported really should point to "< any proof-print member attribute name >"
    # But it's defined manually and correctly so no modification is required.

    keyword = keywords.setdefault(attribute, { 'name': attribute, 'values': [ ], 'specs': [ ],
                                               'syntax': record.find('{*}syntax').text })
    fix_syntax(keyword)
    parse_spec(record.find('{*}xref'), keyword)

    value = record.find('{*}value')
    if value is not None:
        value = value.text

    type = record.find('{*}type')
    if type is not None:
        keyword['type'] = type.text

    # Ignore blank value or values containing stuff like (Reserved)
    if value is None or re.search("\(.*\)", value):
        return

    if ' ' not in value:
        keyword['values'].append(value)
    else:
        if not assign_ref(value, keyword):
            keyword['bad'] = True
            warn("keyword " + attribute + " has unparseable value '" + value + "'")

# Try to figure out what "ref" is referencing, and if successful assign it to target and return True
def assign_ref(ref, target):
    ref = re.sub('^<? ?(any|Any|all|All)? ?', '', ref)
    ref = re.sub(' ?>?$', '', ref)

    # Smash these into the Media type which has everything on earth
    m = re.search('"media" (.*) value', ref)
    if m:
        target['ref'] = 'media'
        target['modifier'] = "any %s" % m.group(1)
        return True

    # XML fix: Correct some known irregularities
    ref = re.sub('"media" color name$', 'media-color name', ref)
    ref = re.sub('job-default-output-until', 'job-delay-output-until', ref)
    ref = re.sub(' the "media-col"$', ' the "media-col" Job Template attribute', ref)
    ref = re.sub(' the "separator-sheets"$', ' the "separator-sheets" Job Template attribute', ref)
    ref = re.sub(' the "cover-back"$', ' the "cover-back" Job Template attribute', ref)
    ref = re.sub(' the "cover-front"$', ' the "cover-front" Job Template attribute', ref)

    # A reference to a keyword
    m = re.search("^\"?([a-z-]+)\"?( |keyword|value(s?)|name(s?))*$", ref)
    if m and m.group(1):
        target['ref'] = m.group(1)
        return True

    # A reference to another attribute
    m = re.search("^\"?([a-z-]+)\"? attribute keyword name$", ref)
    if m and m.group(1):
        target['ref'] = m.group(1)
        return True

    # A reference to any keyword in a group
    m = re.search("^\"?([A-Z a-z-]+)\"? attribute keyword name$", ref)
    if m and m.group(1):
        group = m.group(1)
        # Fix XML
        if group == 'Printer':
            group = 'Printer Description'
        target['ref_group'] = group
        return True

    # A reference to names of members within a collection
    m = re.search("^([a-z-]+) member attribute name$", ref)
    if m and m.group(1):
        target['ref_members'] = m.group(1)
        return True

    m = re.search("^\"([a-z-]+)\"( .* attribute)?$", ref)
    if m and m.group(1):
        target['ref'] = m.group(1)
        return True

    m = re.search("^Member attributes are the same as the \"([a-z-]+)\" (.*) attribute$", ref, re.IGNORECASE)
    if m and m.group(1) and m.group(2):
        target['ref_col'] = m.group(1)
        target['ref_group'] = m.group(2)
        return True

    m = re.search("^Member attributes are the same as \"([a-z-]+)\"$", ref, re.IGNORECASE)
    if m and m.group(1):
        target['ref_col'] = m.group(1)
        return True

    # e.g. "<Any Job Template attribute>"
    m = re.search("([A-Za-z ]+) attribute", ref)
    if m and ' ' in m.group(1):
        target['ref_group'] = m.group(1)
        return True

    return False

crossover_attributes = {
    'system-xri-supported': 'printer-xri-supported',
    'system-contact-col': 'printer-contact-col',
    'system-impressions-completed-col': 'job-impressions-col',
    'system-media-sheets-completed-col': 'job-media-sheets-col',
    'system-pages-completed-col': 'job-pages-col',
    'printer-impressions-completed-col': 'job-impressions-col',
    'printer-media-sheets-completed-col': 'job-media-sheets-col',
    'printer-pages-completed-col': 'job-pages-col',
}

ignored_attributes = [
    # XML Fix (obsolete or maybe just deprecated)
    'document-format-details-supported',
]

# Parse a single attribute record
def parse_attribute(record):
    attr_name = record.find('{*}name').text
    member_name = record.find('{*}member_attribute')
    submember_name = record.find('{*}sub-member_attribute')
    syntax_name = record.find('{*}syntax').text
    collection_name = record.find('{*}collection').text
    xref = record.find('{*}xref')

    collection = attributes.setdefault(collection_name, { })

    # Ignore (UnderReview) (Deprecated) etc
    if re.search("\(.*\)", attr_name):
        attr_name = re.sub(' *\(.*\)', '', attr_name)
        if attr_name in collection:
            del collection[attr_name]
        return

    if attr_name in ignored_attributes:
        return

    attr = collection.setdefault(attr_name, {
        'name': attr_name, 'specs': [ ], 'syntax': syntax_name, 'members': { } } )
    fix_syntax(attr)

    parse_spec(xref, attr)

    if member_name is not None:
        member_name = member_name.text

    # XML fix (no members referenced)
    if member_name is None and attr_name in crossover_attributes:
        member_name = '< Member attributes are the same as "%s" >' % crossover_attributes[attr_name]

    if submember_name is not None:
        submember_name = submember_name.text

    if attr_name == 'printer-xri-requested':
        if member_name:
            warn("printer-xri-requested members are now specified and should be used")
        attr['ref_col'] = 'printer-xri-supported'
        return

    if member_name is not None:
        if member_name.endswith('(extension)'):
            # Chop off (extension) and use it to replace former member syntax
            member_name = member_name[:-len('(extension)')]

        if member_name.endswith('(obsolete)'):
            # This demarcation means we should remove this name
            member_name = member_name[:-len('(obsolete)')]
            if member_name in attr['members']:
                del attr['members'][member_name]
            return

        if member_name.startswith('<'):
            if not assign_ref(member_name, attr):
                warn("Unparseable '" + attr_name + "' member name: '" + member_name + "'")
            return

        attr = attr['members'].setdefault(member_name, {
            'name': member_name, 'specs': [ ], 'syntax': syntax_name, 'members': { }, 'inner': True
        })
        if submember_name is None:
            # Re-apply syntax if member appears again (probably "(extension)")
            attr['syntax'] = syntax_name
        fix_syntax(attr)

        if submember_name is not None:
            if submember_name.startswith('<'):
                if not assign_ref(submember_name, attr):
                    warn("Unparseable '" + attr_name + "' member '" + member_name + "'" +
                         " submember name '" + submember_name + "'")
                return

            attr['members'].setdefault(submember_name, {
                'name': submember_name, 'specs': [ ], 'syntax': syntax_name,
                'members': { } } )
            fix_syntax(attr)

# For each record entity in sections titled with "name", invoke parse()
def parse_records(root, name, parse):
    for elem in root.iter('{*}registry'):
        if elem.find('{*}title').text == name:
            for record in elem.iter('{*}record'):
                parse(record)

def not_numeric(string):
    if string[0].isdigit():
        return "num" + string
    else:
        return string

# Accepts any string, returning in the form Spaced Title
def spaced_title(string):
    return " ".join([word.title() for word in re.split("[ -]", string) if len(word) > 0])

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

def camel_class_path(string):
    return "".join([word.title() for word in re.split("[ _-]", string) if len(word) > 0])

java_keywords = [
    "abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized",
    "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte",
    "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch",
    "extends", "int", "short", "try", "char", "final", "interface", "static", "void", "class", "finally",
    "long", "strictfp", "volatile", "const", "float", "native", "super", "while"
]
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

def upper(string):
    return string.upper()

def depluralize(name):
    if name[-1] != 's':
        return name

    for nonplural in nonplurals:
        if name.endswith(nonplural):
            return name

    return name[:-1]

def emit_keyword(template, keyword):
    # If this is a reference to collection members, we can now resolve it
    if 'ref_members' in keyword:
        if keyword['values']:
            warn("Cannot handle keyword with both ref and values", keyword)
        return

    if not keyword['values'] and 'empty_ok' not in keyword:
        #warn("keyword " + keyword['name'] + " has no values defined", keyword)
        return

    keyword['fullname'] = keyword['name']
    if 'name' in keyword['syntax']:
        keyword['orName'] = True
    keyword['name'] = depluralize(keyword['name'])
    with open(prep_file(keyword['name'], '.java'), "w") as file:
        file.write(rstrip_all(template.render(
            keyword=keyword,
            app=os.path.basename(sys.argv[0]),
            updated=updated,
            specs=specs)))

# Given a class name, select an appropriate location for it and signal the user we are writing it
def prep_file(name, suffix='.kt'):
    out_file = os.path.abspath(proj_dir + 'src/main/java/com/hp/jipp/model/' + camel_class(name) + suffix)
    if not os.path.exists(os.path.dirname(out_file)):
        os.makedirs(os.path.dirname(out_file))
    #print out_file
    if out_file in out_files:
        warn("About to replace " + out_file + ", two competing definitions?")
    out_files.append(out_file)
    return out_file

def emit_enum(template, enum):
    if not enum['values']:
        warn("enum " + enum['name'] + " has no values defined")
        return

    # Remove -supported and -requested
    enum['fullname'] = enum['name']
    enum['name'] = re.sub('-(supported|requested)$', '', enum['name'])
    enum['name'] = depluralize(enum['name'])

    with open(prep_file(enum['name']), "w") as file:
        file.write(rstrip_all(template.render(
            enum=enum,
            app=os.path.basename(sys.argv[0]),
            updated=updated,
            specs=specs)))

def emit_kind(env, template_name, items, emit_func):
    template = env.get_template(template_name)
    for item in list(items.values()):
        if 'bad' in item:
            continue

        if 'ref' in item:
            if item['ref'] not in items:
                warn(item['name'] + " has bad ref=" + item['ref'], item)
            continue

        item['refs'] = []
        for ref_item in list(items.values()):
            if 'ref' in ref_item and ref_item['ref'] == item['name']:
                # Many but not of these refs will be found in Types.*
                item['refs'].append(ref_item['name'])

        emit_func(template, item)

def fuzzy_get(map, name):
    # Given a map, look for name, or something like it, in map
    if name not in map:
        # XML fix: try to find the relevant keyword/enum by trimming
        if name.endswith("-default"):
            return fuzzy_get(map, name[:-len("-default")])
        if name.endswith("-supported"):
            short_name = name[:-len('-supported')]
            if short_name in collections:
                # XML fix: If it's a collection, refer to its members
                return { 'ref_members': short_name }
            else:
                # XML fix: If not, try to find a base (keyword|enum) type of the same name
                return fuzzy_get(map, name[:-len("-supported")])
        if name.endswith("-actual"):
            return fuzzy_get(map, name[:-len("-actual")])
        if name.endswith("-supplied"):
            return fuzzy_get(map, name[:-len("-supplied")])
        if name.endswith("-document-state-reasons"):
            return fuzzy_get(map, "document-state-reasons")
        if name.endswith("-document-state"):
            return fuzzy_get(map, "document-state")
        if name.endswith("-state"):
            return fuzzy_get(map, name + 's') # Try the plural
        if name.endswith("-states") and name[:-1] in map: # Try without the plural
            return map[name[:-1]]
        if name.startswith("output-device-"):
            return fuzzy_get(map, name[len("output-device-"):])
        # XML fix: job-error-sheet-supported needs to look for -type
        if name == "job-error-sheet":
            return fuzzy_get(map, "job-error-sheet-type")
        # XML fix: separator-sheets-supported needs to look for -type
        if name == "separator-sheets":
            return fuzzy_get(map, "separator-sheets-type")
        # XML fix: job-accounting-sheets-supported needs to look for -type
        if name == "job-accounting-sheets":
            return fuzzy_get(map, "job-accounting-sheets-type")
        # XML fix: notify-status-code is a subset of Status.
        if name == 'notify-status-code':
            return fuzzy_get(map, 'status')
        if (name + '-supported') in map:
            return map[name + '-supported']
        return None
    if 'ref' in map[name]:
        return fuzzy_get(map, map[name]['ref'])
    if 'bad' in map[name]:
        return None
    return map[name]

# For the type given, select decorators that help when generating code.
# 'kintro' - string required to begin instantiation of the type
# 'ktype' - the primitive type associated
# 'ktype_accessor' - a way to select out the primitive type from the member
def fix_ktypes(type, syntax, name, group_name = ''):
    original_syntax = syntax

    # These is supposed to refer only to certain Media values but we cannot distinguish them easily.
    if name == 'media-key' or name == 'media-key-supported' or name == "media-size-name":
        name = 'media'

    if syntax is None:
        warn("Type has no syntax", type)
        return None

    set = ".Set" if type.get('set', None) else ""

    # Look for known keyword/enum reference
    intro = None
    if syntax == 'keyword' or syntax == 'keyword | name':
        real_type = fuzzy_get(keywords, name)
        if real_type:
            if 'ref_members' in real_type:
                intro = 'KeywordType%s(' % set
                type['ktype'] = "String"
                type['kdoc'] = "May contain any keyword from [%s.Name]." % camel_class(real_type['ref_members'])
            elif syntax == 'keyword':
                intro = 'KeywordType%s(' % set
                type['ktype'] = "String"
                if real_type['values']:
                    type['kdoc'] = "May contain %s from [%s]." % (keyword_desc(keywords.get(name, None)), camel_class(real_type['name']))
                elif 'kdoc' in real_type:
                    type['kdoc'] = real_type['kdoc']
            elif syntax == 'keyword | name':
                intro = 'KeywordOrNameType%s(' % set
                type['ktype'] = 'KeywordOrName'
                type['kalt'] = [ 'String', 'Name' ]
                type['kdoc'] = "May contain %s from [%s] or a name." % (keyword_desc(keywords.get(name, None)), camel_class(real_type['name']))
        elif syntax == 'keyword':
            # No definition was given so fall back to Keyword
            intro = 'KeywordType%s(' % set
            type['ktype'] = "String"
        elif syntax == 'keyword | name':
            intro = 'KeywordOrNameType%s(' % set
            type['ktype'] = "String"

    if syntax == 'enum':
        real_type = fuzzy_get(enums, name)
        if real_type:
            intro = camel_class(real_type['name']) + '.%sType(' % set[1:]
            type['ktype'] = camel_class(real_type['name'])
            if group_name:
                if 'krefs' not in real_type:
                    real_type['krefs'] = []
                if group_name == 'types':
                    kref = 'Types.' + camel_member(name)
                else:
                    # These are enum types found in collections
                    kref = camel_class_path(group_name) + '.' + camel_member(name)
                if kref not in real_type['krefs']:
                    real_type['krefs'].append(kref)

    if name in key_value_type_names:
        # Catch this before octetString
        intro = 'KeyValuesType%s(' % set
        syntax = "keyValues"
        type['ktype'] = 'KeyValues'
        type['kref'] = camel_class_path(group_name) + '.' + camel_member(name)
        key_values[name] = type

    # Look for other known references
    if re.search('^uri(\([0-9]+\))?$', syntax):
        intro = 'UriType%s(' % set
        type['ktype'] = "java.net.URI"
    elif re.search('^rangeOfInteger(\([0-9MINAX:-]*\))?$', syntax):
        intro = 'IntRangeType%s(' % set
        type['ktype'] = 'IntRange'
    elif re.search('^integer(\([0-9MINAX: -]*\)) | rangeOfInteger(\([0-9MINAX: -]*\))?$', syntax):
        intro = 'IntOrIntRangeType%s(' % set
        type['ktype'] = 'IntOrIntRange'
        type['kalt'] = [ 'Int', 'IntRange' ]
    elif re.search('^integer(\([0-9MINAX: -]*\))?$', syntax):
        intro = 'IntType%s(' % set
        type['ktype'] = "Int"
    elif syntax == "boolean":
        intro = 'BooleanType%s(' % set
        type['ktype'] = "Boolean"
    elif syntax == "charset":
        intro = 'StringType%s(Tag.charset, ' % set
        type['ktype'] = "String"
    elif syntax == "mimeMediaType":
        intro = 'StringType%s(Tag.mimeMediaType, ' % set
        type['ktype'] = "String"
    elif syntax == "naturalLanguage":
        intro = 'StringType%s(Tag.naturalLanguage, ' % set
    elif syntax == "resolution":
        intro = 'ResolutionType%s(' % set
        type['ktype'] = 'Resolution'
    elif syntax == "collection":
        if 'ref_col' in type:
            name = type['ref_col']
        else:
            name = type['name']

        # Some collections are beyond our ability to model cleanly, having too many possibilities
        if name == 'destination-attributes' or name == 'preferred-attributes':
            intro = 'UntypedCollection.%sType(' % set[1:]
            type['ktype'] = 'UntypedCollection'
        else:
            intro = 'AttributeCollection.%sType(' % set[1:]
            type['koutro'] = ", %s" % camel_class(name)
            type['ktype'] = camel_class(name)
            if not 'inner' in type:
                # Non-inner collection references must be fulfilled later
                pending_collections[name] = type
    elif syntax == "dateTime":
        intro = 'DateTimeType%s(' % set
        type['ktype'] = "java.util.Calendar"
    elif syntax == 'name':
        intro = 'NameType%s(' % set
    elif re.search('^name\(([0-9]+)\)$', syntax):
        m = re.search('name\(([0-9]+)\)', syntax) # ignore max (for now)
        intro = 'NameType%s(' % set
    elif syntax == 'text':
        intro = 'TextType%s(' % set
    elif syntax == 'uriScheme':
        intro = 'StringType%s(Tag.uriScheme, ' % set
    elif re.search('^text\(([0-9]+)\)$', syntax):
        m = re.search('text\(([0-9]+)\)', syntax) # ignore max (for now)
        intro = 'TextType%s(' % set
    elif syntax == 'octetString':
        intro = 'OctetsType%s(' % set
    elif re.search('^octetString\(([0-9]+)\)$', syntax):
        m = re.search('octetString\(([0-9]+)\)', syntax) # ignore max (for now)
        intro = 'OctetsType%s(' % set

    if not intro:
        warn("No type for attribute " + name + " with syntax '" + original_syntax + "'", type)
        return

    type['kintro'] = intro
    if intro.startswith("StringType"):
        type['ktype'] = "String"
    elif intro.startswith("NameType"):
        type['ktype'] = "String"
        type['ktype_accessor'] = "value"
        type['kbase'] = "Name"
    elif intro.startswith("TextType"):
        type['ktype'] = "String"
        type['ktype_accessor'] = "value"
        type['kbase'] = "Text"
    elif intro.startswith("OctetsType"):
        type['ktype'] = "ByteArray"

def emit_attributes(env):
    # Pass 1: Look for collection types that have both ref and members
    for group in list(attributes.values()):
        for type in list(group.values()):
            if 'ref_col' in type and type['ref_col'] and type['members']:
                referent = attributes[type['ref_group']][type['ref_col']]
                # Push members over to referent
                for new_member in list(type['members'].values()):
                    if new_member['name'] in referent['members'] and \
                            referent['members'][new_member['name']] != new_member:
                        warn("Collection type already has different member " + new_member['name'], referent)
                else:
                    referent['members'][new_member['name']] = new_member
                type['members'] = { }

    # Pass 2: Emit collection types having members
    for group in list(attributes.values()):
        for type in list(group.values()):
            if type['members']:
                emit_collection(env, type)

    # Pass 3: handle any collections without members or refs
    for group in list(attributes.values()):
        for type in list(group.values()):
            if type['syntax'] == 'collection':
                handle_collection_ref(group, type)

    # Pass 4: Warn about any collection references that have not been handled
    for key, value in list(pending_collections.items()):
        if key not in collections:
            warn("Collection " + key + " referenced but not found", value)
        elif 'emitted' not in collections[key]:
            warn("Collection " + key + " referenced but not emitted", value)

    # Pass 5: Make sure types have consistent definitions
    types = { }
    for group_name, values in list(attributes.items()):
        for name, type in list(values.items()):
            if name in types:
                # Update the old type with new data or warn
                old_type = types[name]
                if type['syntax'] == 'collection' and old_type['name'] == type['name']:
                    old_type['specs'] = sorted(set(old_type['specs'] + type['specs']))
                    type = None
                elif set([type['syntax'], old_type['syntax']]) == set(['name', 'keyword | name']):
                    old_type['specs'] = sorted(set(old_type['specs'] + type['specs']))
                    old_type['syntax'] = 'keyword | name'
                    type = None
                elif set([type['syntax'], old_type['syntax']]) == set(['uri', 'uri(45)']):
                    old_type['specs'] = sorted(set(old_type['specs'] + type['specs']))
                    old_type['syntax'] = 'uri'
                    type = None
                elif set([type['syntax'], old_type['syntax']]) == set(['integer', 'integer(1:MAX)']):
                    old_type['specs'] = sorted(set(old_type['specs'] + type['specs']))
                    old_type['syntax'] = 'integer'
                    type = None
                elif old_type.get('set', None) != type.get('set', None):
                    # One is a set, one is not (e.g. printer-service-type), so just consider them both sets
                    old_type['set'] = True
                    type = None
                elif no_specs(old_type) != no_specs(type):
                    warn("Type repeated with differences in " + group_name, [types[name], type])
                    type = None

            if type:
                types[name] = type
                # type = copy.deepcopy(type)
                # fix_ktypes(type, type['syntax'], type['name'], group_name + "-group")
                # if 'kintro' not in type:
                #     # fix_ktypes already warns
                #     continue

    # Pass 6: Emit all types into a single huge class
    type_list = []
    for typeName, type in sorted(list(types.items()), key=lambda k_v: k_v[0]):
        fix_ktypes(type, type['syntax'], type['name'], 'types')
        if 'kintro' not in type:
            # fix_ktypes already warns
            continue

        # Fix members
        for member in list(type['members'].values()):
            fix_member(member, '')

        type_list.append(type)

    with open(prep_file('types'), 'w') as file:
        template = env.get_template('types.kt.tmpl')
        file.write(rstrip_all(template.render(
            types=type_list,
            app=os.path.basename(sys.argv[0]),
            updated=updated,
            specs=specs)))

def no_specs(type):
    type = copy.deepcopy(type)
    del type['specs']
    return type

# Make sure collection type has values or at least a ref_col in it. If not, delete type from group.
def handle_collection_ref(group, type):
    if type['members'] or 'ref_col' in type:
        # It's already OK
        return

    if type['name'] in collections:
        # Fix XML: We have the collection already, nevermind (e.g. job-media-sheets-col)
        return

    if type['name'].endswith('-actual'):
        # Fix XML: We'll assume these are the same as the root collection
        name = type['name'][:-len('-actual')]
        if name in collections:
            type['ref_col'] = name
            return

    if type['name'].endswith('-default'):
        # Fix XML: We'll assume these are the same as the root collection
        name = type['name'][:-len('-default')]
        if name in collections:
            type['ref_col'] = name
            return

    if '-completed-' in type['name']:
        name = type['name'].replace('-completed-', '-')
        if name in collections:
            type['ref_col'] = name
            return

    if type['name'].startswith("materials-col-"):
        # Fix XML: materials-col-ready, -supported, -database don't reference materials-col directly.
        type['ref_col'] = 'materials-col'
        return

    if type['name'].startswith("finishings-col-"):
        # Fix XML: finishings-col-database doesn't reference finishings-col directly
        type['ref_col'] = 'finishings-col'
        return

    # We have a solution in place for this elsewhere
    if type['name'] == 'preferred-attributes':
        return

    del group[type['name']]

    # Fix XML: document-overrides is from 5100.4 and is now obsolete. This should be deprecated.
    if type['name'] == 'document-overrides-actual':
        return

    warn('no members found for collection', type)

def emit_collection(env, type):
    original_type = type
    collection_template = env.get_template('collection.kt.tmpl')
    name = type['name']

    if name in collections:
        if collections[name]['members'] != type['members']:
            # Look at the new members in the un-emitted version: are they already represented?
            for member_name in type['members']:
                emitted_member = collections[name]['members'].get(member_name, None)
                new_member = type['members'][member_name]
                if emitted_member != new_member:
                    warn("Collection %s has a mismatched member: emitted %s vs %s" % (name, emitted_member, new_member))
            return
        else:
            # Already done a matching one so skip
            return
    collections[name] = type

    type = copy.deepcopy(type)
    for member in list(type['members'].values()):
        fix_member(member, name)
        if 'ktype' not in member:
            warn("Collection " + name + ' member ' + member['name'] + ' has no ktype for ' +
                 member['syntax'], member)
            del collections[name]
            return

        # Create inner class for each member that needs one
        if member['members']:
            member['kimpl'] = '    ' + collection_template.render(
                name=member['name'], collection=member, app=os.path.basename(sys.argv[0]), updated=updated,
                specs=specs, noheader=True).replace('\n', '\n    ').strip()

    original_type['emitted'] = True
    with open(prep_file(name), 'w') as file:
        file.write(rstrip_all(collection_template.render(
            name=name, collection=type, app=os.path.basename(sys.argv[0]),
            updated=updated, specs=specs)))

def rstrip_all(text):
    return re.sub(' +\n', '\n', text)

# For each member recursively find and apply its intro
def fix_member(member, group_name):
    fix_syntax(member)
    fix_ktypes(member, member['syntax'], member['name'], group_name)
    if 'kintro' in member:
        if member['syntax'] == 'dateTime':
            if 'ktype' not in member:
                member['ktype'] = 'java.util.Calendar'
        if member['syntax'] == 'collection':
            if 'ktype' not in member:
                member['ktype'] = camel_class(member['name'])
            if member['members']:
                for submember in list(member['members'].values()):
                    if group_name:
                        fix_member(submember, group_name + '.' + member['name'])
                    else:
                        fix_member(submember, '')

def keyword_desc(type):
    if type and 'modifier' in type:
        return "%s keyword" % type['modifier']
    return "any keyword"

def emit_code():
    env = Environment(loader=FileSystemLoader(proj_dir + 'bin'))
    env.filters['camel_class'] = camel_class
    env.filters['camel_member'] = camel_member
    env.filters['spaced_title'] = spaced_title
    env.filters['upper'] = upper

    emit_kind(env, 'enum.kt.tmpl', enums, emit_enum)
    emit_kind(env, 'keyword.java.tmpl', keywords, emit_keyword)

    emit_attributes(env)

    with open(prep_file('Enum-Types'), 'w') as file:
        file.write(rstrip_all(env.get_template('enums.kt.tmpl').render(
            enums=[kref for enum in list(enums.values()) if 'krefs' in enum for kref in enum['krefs'] ],
            app=os.path.basename(sys.argv[0]),
            updated=updated)))

    with open(prep_file('Key-Values-Types'), 'w') as file:
        file.write(rstrip_all(env.get_template('keyvalues.kt.tmpl').render(
            types=list(key_values.values()),
            app=os.path.basename(sys.argv[0]),
            updated=updated)))

# MAIN

xml_file = proj_dir + 'build/ipp-registrations.xml'
if not os.path.exists(os.path.dirname(xml_file)):
    os.makedirs(os.path.dirname(xml_file))

# Fetch the file into xml_file if necessary
if not os.path.isfile(xml_file):
    urllib.request.urlretrieve('http://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml', xml_file)

# Parse from file
tree = etree.parse(xml_file)

# Capture the update date to be added to all files
for elem in tree.iter('{*}registry'):
    if elem.find('{*}title').text == "Internet Printing Protocol (IPP) Registrations":
        updated = elem.find('{*}updated').text

parse_records(tree, "Enum Attribute Values", parse_enum)
parse_records(tree, "Keyword Attribute Values", parse_keyword)

# XML Fix: missing printer-kind keyword
keywords['printer-kind'] = {
    'name': 'printer-kind',
    'specs': [ 'PWG5100.16'],
    'syntax': 'keyword',
    'values' : [ 'disc', 'document', 'envelope', 'label', 'large-format', 'photo', 'postcard', 'receipt', 'roll']
}

# XML Fix: preset-name not in keywords listing because no values are defined for it.
keywords['preset-name'] = {
    'name': 'preset-name',
    'specs': [ 'IPPPRESET'],
    'syntax': 'keyword | name',
    'values' : [ ],
    'empty_ok' : True
}

# XML Fix: not listed with other keywords
keywords['destination-mandatory-access-attributes'] = {
    'name': 'destination-mandatory-access-attributes',
    'specs': 'PWG5100.17',
    'syntax': 'keyword',
    'ref_members': 'destination-accesses',
    'values': [ ]
}

# XML limit: printer-settable-attributes-supported keyword includes 'none' and the names of all possible
# settiable attributes of the printer; too many to list here.
keywords['printer-settable-attributes-supported'] = {
    'name': 'printer-settable-attributes-supported',
    'specs': [ 'RFC3380' ],
    'syntax': 'keyword',
    'values': [ 'none' ]
}

# XML limit: job-settable-attributes-supported keyword includes 'none' and the names of all possible
# attributes which can be set on a job operation; too many to list here.
keywords['job-settable-attributes-supported'] = {
    'name': 'job-settable-attributes-supported',
    'specs': [ 'RFC3380' ],
    'syntax': 'keyword',
    'values': [ 'none' ]
}

parse_records(tree, "Attributes", parse_attribute)
parse_records(tree, "Status Codes", parse_status_code)

# XML Fix: finishings-col has media-size but is not specified (should be same as media-col.media-size)
attributes['Job Template']['finishings-col']['members']['media-size'] = \
    copy.deepcopy(attributes['Job Template']['media-col']['members']['media-size'])

emit_code()

if warns:
    print("WARNINGS: " + str(warns))
else:
    print("No warnings")

