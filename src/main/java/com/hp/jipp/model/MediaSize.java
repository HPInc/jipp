package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeType;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AutoValue
public abstract class MediaSize {
    private static final Pattern WIDTH_HEIGHT = Pattern.compile(
            "_([0-9]+(\\.[0-9]+)?)?x([0-9]+(\\.[0-9]+)?)([a-z]+)?$");
    private static final String TYPE_NAME = "MediaSize";
    private static final int MM_HUNDREDTHS_PER_INCH = 2540;
    private static final int MM_HUNDREDTHS_PER_MM = 100;

    public static final MediaSize Iso2a0 = of("iso_2a0_1189x1682mm");
    public static final MediaSize IsoA0 = of("iso_a0_841x1189mm");
    public static final MediaSize IsoA0x3 = of("iso_a0x3_1189x2523mm");
    public static final MediaSize IsoA10 = of("iso_a10_26x37mm");
    public static final MediaSize IsoA1 = of("iso_a1_594x841mm");
    public static final MediaSize IsoA1x3 = of("iso_a1x3_841x1783mm");
    public static final MediaSize IsoA1x4 = of("iso_a1x4_841x2378mm");
    public static final MediaSize IsoA2 = of("iso_a2_420x594mm");
    public static final MediaSize IsoA2x3 = of("iso_a2x3_594x1261mm");
    public static final MediaSize IsoA2x4 = of("iso_a2x4_594x1682mm");
    public static final MediaSize IsoA2x5 = of("iso_a2x5_594x2102mm");
    public static final MediaSize IsoA3 = of("iso_a3_297x420mm");
    public static final MediaSize IsoA3Extra = of("iso_a3-extra_322x445mm");
    public static final MediaSize IsoA3x3 = of("iso_a3x3_420x891mm");
    public static final MediaSize IsoA3x4 = of("iso_a3x4_420x1189mm");
    public static final MediaSize IsoA3x5 = of("iso_a3x5_420x1486mm");
    public static final MediaSize IsoA3x6 = of("iso_a3x6_420x1783mm");
    public static final MediaSize IsoA3x7 = of("iso_a3x7_420x2080mm");
    public static final MediaSize IsoA4 = of("iso_a4_210x297mm");
    public static final MediaSize IsoA4Extra = of("iso_a4-extra_235.5x322.3mm");
    public static final MediaSize IsoA4Tab = of("iso_a4-tab_225x297mm");
    public static final MediaSize IsoA4x3 = of("iso_a4x3_297x630mm");
    public static final MediaSize IsoA4x4 = of("iso_a4x4_297x841mm");
    public static final MediaSize IsoA4x5 = of("iso_a4x5_297x1051mm");
    public static final MediaSize IsoA4x6 = of("iso_a4x6_297x1261mm");
    public static final MediaSize IsoA4x7 = of("iso_a4x7_297x1471mm");
    public static final MediaSize IsoA4x8 = of("iso_a4x8_297x1682mm");
    public static final MediaSize IsoA4x9 = of("iso_a4x9_297x1892mm");
    public static final MediaSize IsoA5 = of("iso_a5_148x210mm");
    public static final MediaSize IsoA5Extra = of("iso_a5-extra_174x235mm");
    public static final MediaSize IsoA6 = of("iso_a6_105x148mm");
    public static final MediaSize IsoA7 = of("iso_a7_74x105mm");
    public static final MediaSize IsoA8 = of("iso_a8_52x74mm");
    public static final MediaSize IsoA9 = of("iso_a9_37x52mm");
    public static final MediaSize IsoB0 = of("iso_b0_1000x1414mm");
    public static final MediaSize IsoB10 = of("iso_b10_31x44mm");
    public static final MediaSize IsoB1 = of("iso_b1_707x1000mm");
    public static final MediaSize IsoB2 = of("iso_b2_500x707mm");
    public static final MediaSize IsoB3 = of("iso_b3_353x500mm");
    public static final MediaSize IsoB4 = of("iso_b4_250x353mm");
    public static final MediaSize IsoB5 = of("iso_b5_176x250mm");
    public static final MediaSize IsoB5Extra = of("iso_b5-extra_201x276mm");
    public static final MediaSize IsoB6 = of("iso_b6_125x176mm");
    public static final MediaSize IsoB6c4 = of("iso_b6c4_125x324mm");
    public static final MediaSize IsoB7 = of("iso_b7_88x125mm");
    public static final MediaSize IsoB8 = of("iso_b8_62x88mm");
    public static final MediaSize IsoB9 = of("iso_b9_44x62mm");
    public static final MediaSize IsoC0 = of("iso_c0_917x1297mm");
    public static final MediaSize IsoC10 = of("iso_c10_28x40mm");
    public static final MediaSize IsoC1 = of("iso_c1_648x917mm");
    public static final MediaSize IsoC2 = of("iso_c2_458x648mm");
    public static final MediaSize IsoC3 = of("iso_c3_324x458mm");
    public static final MediaSize IsoC4 = of("iso_c4_229x324mm");
    public static final MediaSize IsoC5 = of("iso_c5_162x229mm");
    public static final MediaSize IsoC6 = of("iso_c6_114x162mm");
    public static final MediaSize IsoC6c5 = of("iso_c6c5_114x229mm");
    public static final MediaSize IsoC7 = of("iso_c7_81x114mm");
    public static final MediaSize IsoC7c6 = of("iso_c7c6_81x162mm");
    public static final MediaSize IsoC8 = of("iso_c8_57x81mm");
    public static final MediaSize IsoC9 = of("iso_c9_40x57mm");
    public static final MediaSize IsoDl = of("iso_dl_110x220mm");
    public static final MediaSize IsoRa0 = of("iso_ra0_860x1220mm");
    public static final MediaSize IsoRa1 = of("iso_ra1_610x860mm");
    public static final MediaSize IsoRa2 = of("iso_ra2_430x610mm");
    public static final MediaSize IsoRa3 = of("iso_ra3_305x430mm");
    public static final MediaSize IsoRa4 = of("iso_ra4_215x305mm");
    public static final MediaSize IsoSra0 = of("iso_sra0_900x1280mm");
    public static final MediaSize IsoSra1 = of("iso_sra1_640x900mm");
    public static final MediaSize IsoSra2 = of("iso_sra2_450x640mm");
    public static final MediaSize IsoSra3 = of("iso_sra3_320x450mm");
    public static final MediaSize IsoSra4 = of("iso_sra4_225x320mm");
    public static final MediaSize JisB0 = of("jis_b0_1030x1456mm");
    public static final MediaSize JisB10 = of("jis_b10_32x45mm");
    public static final MediaSize JisB1 = of("jis_b1_728x1030mm");
    public static final MediaSize JisB2 = of("jis_b2_515x728mm");
    public static final MediaSize JisB3 = of("jis_b3_364x515mm");
    public static final MediaSize JisB4 = of("jis_b4_257x364mm");
    public static final MediaSize JisB5 = of("jis_b5_182x257mm");
    public static final MediaSize JisB6 = of("jis_b6_128x182mm");
    public static final MediaSize JisB7 = of("jis_b7_91x128mm");
    public static final MediaSize JisB8 = of("jis_b8_64x91mm");
    public static final MediaSize JisB9 = of("jis_b9_45x64mm");
    public static final MediaSize JisExec = of("jis_exec_216x330mm");
    public static final MediaSize JpnChou2 = of("jpn_chou2_111.1x146mm");
    public static final MediaSize JpnChou3 = of("jpn_chou3_120x235mm");
    public static final MediaSize JpnChou40 = of("jpn_chou40_90x225mm");
    public static final MediaSize JpnChou4 = of("jpn_chou4_90x205mm");
    public static final MediaSize JpnHagaki = of("jpn_hagaki_100x148mm");
    public static final MediaSize JpnKahu = of("jpn_kahu_240x322.1mm");
    public static final MediaSize JpnKaku2 = of("jpn_kaku2_240x332mm");
    public static final MediaSize JpnKaku3 = of("jpn_kaku3_216x277mm");
    public static final MediaSize JpnKaku4 = of("jpn_kaku4_197x267mm");
    public static final MediaSize JpnKaku5 = of("jpn_kaku5_190x240mm");
    public static final MediaSize JpnKaku7 = of("jpn_kaku7_142x205mm");
    public static final MediaSize JpnKaku8 = of("jpn_kaku8_119x197mm");
    public static final MediaSize JpnOufuku = of("jpn_oufuku_148x200mm");
    public static final MediaSize JpnYou4 = of("jpn_you4_105x235mm");
    public static final MediaSize JpnYou6 = of("jpn_you6_98x190mm");
    public static final MediaSize Na10x11 = of("na_10x11_10x11in");
    public static final MediaSize Na10x13 = of("na_10x13_10x13in");
    public static final MediaSize Na10x14 = of("na_10x14_10x14in");
    public static final MediaSize Na10x15 = of("na_10x15_10x15in");
    public static final MediaSize Na11x12 = of("na_11x12_11x12in");
    public static final MediaSize Na11x15 = of("na_11x15_11x15in");
    public static final MediaSize Na12x19 = of("na_12x19_12x19in");
    public static final MediaSize Na5x7 = of("na_5x7_5x7in");
    public static final MediaSize Na6x9 = of("na_6x9_6x9in");
    public static final MediaSize Na7x9 = of("na_7x9_7x9in");
    public static final MediaSize Na9x11 = of("na_9x11_9x11in");
    public static final MediaSize NaA2 = of("na_a2_4.375x5.75in");
    public static final MediaSize NaArchA = of("na_arch-a_9x12in");
    public static final MediaSize NaArchB = of("na_arch-b_12x18in");
    public static final MediaSize NaArchC = of("na_arch-c_18x24in");
    public static final MediaSize NaArchD = of("na_arch-d_24x36in");
    public static final MediaSize NaArchE = of("na_arch-e_36x48in");
    public static final MediaSize NaBPlus = of("na_b-plus_12x19.17in");
    public static final MediaSize NaC = of("na_c_17x22in");
    public static final MediaSize NaC5 = of("na_c5_6.5x9.5in");
    public static final MediaSize NaD = of("na_d_22x34in");
    public static final MediaSize NaE = of("na_e_34x44in");
    public static final MediaSize NaEdp = of("na_edp_11x14in");
    public static final MediaSize NaEurEdp = of("na_eur-edp_12x14in");
    public static final MediaSize NaExecutive = of("na_executive_7.25x10.5in");
    public static final MediaSize NaF = of("na_f_44x68in");
    public static final MediaSize NaFanfoldEur = of("na_fanfold-eur_8.5x12in");
    public static final MediaSize NaFanfoldUs = of("na_fanfold-us_11x14.875in");
    public static final MediaSize NaFoolscap = of("na_foolscap_8.5x13in");
    public static final MediaSize NaGovtLegal = of("na_govt-legal_8x13in");
    public static final MediaSize NaGovtLetter = of("na_govt-letter_8x10in");
    public static final MediaSize NaIndex3x5 = of("na_index-3x5_3x5in");
    public static final MediaSize NaIndex4x6 = of("na_index-4x6_4x6in");
    public static final MediaSize NaIndex4x6Ext = of("na_index-4x6-ext_6x8in");
    public static final MediaSize NaIndex5x8 = of("na_index-5x8_5x8in");
    public static final MediaSize NaInvoice = of("na_invoice_5.5x8.5in");
    public static final MediaSize NaLedger = of("na_ledger_11x17in");
    public static final MediaSize NaLegal = of("na_legal_8.5x14in");
    public static final MediaSize NaLegalExtra = of("na_legal-extra_9.5x15in");
    public static final MediaSize NaLetter = of("na_letter_8.5x11in");
    public static final MediaSize NaLetterExtra = of("na_letter-extra_9.5x12in");
    public static final MediaSize NaLetterPlus = of("na_letter-plus_8.5x12.69in");
    public static final MediaSize NaMonarch = of("na_monarch_3.875x7.5in");
    public static final MediaSize NaNumber10 = of("na_number-10_4.125x9.5in");
    public static final MediaSize NaNumber11 = of("na_number-11_4.5x10.375in");
    public static final MediaSize NaNumber12 = of("na_number-12_4.75x11in");
    public static final MediaSize NaNumber14 = of("na_number-14_5x11.5in");
    public static final MediaSize NaNumber9 = of("na_number-9_3.875x8.875in");
    public static final MediaSize NaOficio = of("na_oficio_8.5x13.4in");
    public static final MediaSize NaPersonal = of("na_personal_3.625x6.5in");
    public static final MediaSize NaQuarto = of("na_quarto_8.5x10.83in");
    public static final MediaSize NaSuperA = of("na_super-a_8.94x14in");
    public static final MediaSize NaSuperB = of("na_super-b_13x19in");
    public static final MediaSize NaWideFormat = of("na_wide-format_30x42in");
    public static final MediaSize OmDaiPaKai = of("om_dai-pa-kai_275x395mm");
    public static final MediaSize OmFolio = of("om_folio_210x330mm");
    public static final MediaSize OmFolioSp = of("om_folio-sp_215x315mm");
    public static final MediaSize OmInvite = of("om_invite_220x220mm");
    public static final MediaSize OmItalian = of("om_italian_110x230mm");
    public static final MediaSize OmJuuroKuKai = of("om_juuro-ku-kai_198x275mm");
    public static final MediaSize OmLargePhoto = of("om_large-photo_200x300");
    public static final MediaSize OmMediumPhoto = of("om_medium-photo_130x180mm");
    public static final MediaSize OnPaKai = of("om_pa-kai_267x389mm");
    public static final MediaSize OmPostfix = of("om_postfix_114x229mm");
    public static final MediaSize OmSmallPhoto = of("om_small-photo_100x150mm");
    public static final MediaSize OmWidePhoto = of("om_wide-photo_100x200mm");
    public static final MediaSize Prc10 = of("prc_10_324x458mm");
    public static final MediaSize Prc1 = of("prc_1_102x165mm");
    public static final MediaSize Prc16k = of("prc_16k_146x215mm");
    public static final MediaSize Prc2 = of("prc_2_102x176mm");
    public static final MediaSize Prc3 = of("prc_3_125x176mm");
    public static final MediaSize Prc32k = of("prc_32k_97x151mm");
    public static final MediaSize Prc4 = of("prc_4_110x208mm");
    public static final MediaSize Prc6 = of("prc_6_120x320mm");
    public static final MediaSize Prc7 = of("prc_7_160x230mm");
    public static final MediaSize Prc8 = of("prc_8_120x309mm");
    public static final MediaSize Roc16k = of("roc_16k_7.75x10.75in");
    public static final MediaSize Roc8k = of("roc_8k_10.75x15.5in");

    private static final Map<String, MediaSize> All;

    static {
        // Look up all MediaSizes defined here
        ImmutableMap.Builder<String, MediaSize> all = new ImmutableMap.Builder<>();
        for (Object object : Util.getStaticObjects(MediaSize.class)) {
            if (MediaSize.class.isAssignableFrom(object.getClass())) {
                all.put(((MediaSize) object).getName(), (MediaSize) object);
            }
        }
        All = all.build();
    }

    public static MediaSize of(String name, int x, int y) {
        return new AutoValue_MediaSize(name, x, y);
    }

    private static MediaSize of(String name) {
        Matcher matches = WIDTH_HEIGHT.matcher(name);
        if (!matches.find() || matches.groupCount() < 4) {
            // No way to guess media size from name
            return of(name, 0, 0);
        }

        // Assume mm
        int units = MM_HUNDREDTHS_PER_MM;
        if (matches.groupCount() >= 5 && matches.group(5) != null && matches.group(5).equals("in")) {
            units = MM_HUNDREDTHS_PER_INCH;
        }

        int x = (int) (Double.parseDouble(matches.group(1)) * units);
        int y = (int) (Double.parseDouble(matches.group(3)) * units);
        return of(name, x, y);
    }

    /**
     * Self-Describing name as per <a href="https://ftp.pwg.org/pub/pwg/candidates/cs-pwgmsn20-20130328-5101.1.pdf">
     * PWG5101.1</a>
     */
    public abstract String getName();


    /** Width of media in 1/100 of a millimeter or 1/2540 of an inch */
    public abstract int getWidth();

    /** Height of media in 1/100 of a millimeter or 1/2540 of an inch */
    public abstract int getHeight();

    static final Attribute.Encoder<MediaSize> ENCODER = new Attribute.Encoder<MediaSize>(TYPE_NAME) {
        @Override
        public MediaSize readValue(DataInputStream in, Tag valueTag) throws IOException {
            String name = StringType.ENCODER.readValue(in, valueTag);
            if (All.containsKey(name)) {
                return All.get(name);
            }
            return MediaSize.of(name);
        }

        @Override
        public void writeValue(DataOutputStream out, MediaSize value) throws IOException {
            StringType.ENCODER.writeValue(out, value.getName());
        }

        @Override
        public boolean valid(Tag valueTag) {
            return Tag.Keyword.equals(valueTag) || Tag.NameWithoutLanguage.equals(valueTag);
        }
    };

    /** A media size type based solely on keyword values with width/height inferred */
    public static final class Type extends AttributeType<MediaSize> {
        public Type(String name) {
            super(ENCODER, Tag.Keyword, name);
        }
    }
}
