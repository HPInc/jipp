package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeType;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AutoValue
public abstract class MediaSize {
    private final static Pattern WIDTH_HEIGHT = Pattern.compile("_([0-9]+(\\.[0-9]+)?)?x([0-9]+(\\.[0-9]+)?)([a-z]+)?$");
    private final static String TYPE_NAME = "MediaSize";

    public final static MediaSize Iso2a0 = of("iso_2a0_1189x1682mm");
    public final static MediaSize IsoA0 = of("iso_a0_841x1189mm");
    public final static MediaSize IsoA0x3 = of("iso_a0x3_1189x2523mm");
    public final static MediaSize IsoA10 = of("iso_a10_26x37mm");
    public final static MediaSize IsoA1 = of("iso_a1_594x841mm");
    public final static MediaSize IsoA1x3 = of("iso_a1x3_841x1783mm");
    public final static MediaSize IsoA1x4 = of("iso_a1x4_841x2378mm");
    public final static MediaSize IsoA2 = of("iso_a2_420x594mm");
    public final static MediaSize IsoA2x3 = of("iso_a2x3_594x1261mm");
    public final static MediaSize IsoA2x4 = of("iso_a2x4_594x1682mm");
    public final static MediaSize IsoA2x5 = of("iso_a2x5_594x2102mm");
    public final static MediaSize IsoA3 = of("iso_a3_297x420mm");
    public final static MediaSize IsoA3Extra = of("iso_a3-extra_322x445mm");
    public final static MediaSize IsoA3x3 = of("iso_a3x3_420x891mm");
    public final static MediaSize IsoA3x4 = of("iso_a3x4_420x1189mm");
    public final static MediaSize IsoA3x5 = of("iso_a3x5_420x1486mm");
    public final static MediaSize IsoA3x6 = of("iso_a3x6_420x1783mm");
    public final static MediaSize IsoA3x7 = of("iso_a3x7_420x2080mm");
    public final static MediaSize IsoA4 = of("iso_a4_210x297mm");
    public final static MediaSize IsoA4Extra = of("iso_a4-extra_235.5x322.3mm");
    public final static MediaSize IsoA4Tab = of("iso_a4-tab_225x297mm");
    public final static MediaSize IsoA4x3 = of("iso_a4x3_297x630mm");
    public final static MediaSize IsoA4x4 = of("iso_a4x4_297x841mm");
    public final static MediaSize IsoA4x5 = of("iso_a4x5_297x1051mm");
    public final static MediaSize IsoA4x6 = of("iso_a4x6_297x1261mm");
    public final static MediaSize IsoA4x7 = of("iso_a4x7_297x1471mm");
    public final static MediaSize IsoA4x8 = of("iso_a4x8_297x1682mm");
    public final static MediaSize IsoA4x9 = of("iso_a4x9_297x1892mm");
    public final static MediaSize IsoA5 = of("iso_a5_148x210mm");
    public final static MediaSize IsoA5Extra = of("iso_a5-extra_174x235mm");
    public final static MediaSize IsoA6 = of("iso_a6_105x148mm");
    public final static MediaSize IsoA7 = of("iso_a7_74x105mm");
    public final static MediaSize IsoA8 = of("iso_a8_52x74mm");
    public final static MediaSize IsoA9 = of("iso_a9_37x52mm");
    public final static MediaSize IsoB0 = of("iso_b0_1000x1414mm");
    public final static MediaSize IsoB10 = of("iso_b10_31x44mm");
    public final static MediaSize IsoB1 = of("iso_b1_707x1000mm");
    public final static MediaSize IsoB2 = of("iso_b2_500x707mm");
    public final static MediaSize IsoB3 = of("iso_b3_353x500mm");
    public final static MediaSize IsoB4 = of("iso_b4_250x353mm");
    public final static MediaSize IsoB5 = of("iso_b5_176x250mm");
    public final static MediaSize IsoB5Extra = of("iso_b5-extra_201x276mm");
    public final static MediaSize IsoB6 = of("iso_b6_125x176mm");
    public final static MediaSize IsoB6c4 = of("iso_b6c4_125x324mm");
    public final static MediaSize IsoB7 = of("iso_b7_88x125mm");
    public final static MediaSize IsoB8 = of("iso_b8_62x88mm");
    public final static MediaSize IsoB9 = of("iso_b9_44x62mm");
    public final static MediaSize IsoC0 = of("iso_c0_917x1297mm");
    public final static MediaSize IsoC10 = of("iso_c10_28x40mm");
    public final static MediaSize IsoC1 = of("iso_c1_648x917mm");
    public final static MediaSize IsoC2 = of("iso_c2_458x648mm");
    public final static MediaSize IsoC3 = of("iso_c3_324x458mm");
    public final static MediaSize IsoC4 = of("iso_c4_229x324mm");
    public final static MediaSize IsoC5 = of("iso_c5_162x229mm");
    public final static MediaSize IsoC6 = of("iso_c6_114x162mm");
    public final static MediaSize IsoC6c5 = of("iso_c6c5_114x229mm");
    public final static MediaSize IsoC7 = of("iso_c7_81x114mm");
    public final static MediaSize IsoC7c6 = of("iso_c7c6_81x162mm");
    public final static MediaSize IsoC8 = of("iso_c8_57x81mm");
    public final static MediaSize IsoC9 = of("iso_c9_40x57mm");
    public final static MediaSize IsoDl = of("iso_dl_110x220mm");
    public final static MediaSize IsoRa0 = of("iso_ra0_860x1220mm");
    public final static MediaSize IsoRa1 = of("iso_ra1_610x860mm");
    public final static MediaSize IsoRa2 = of("iso_ra2_430x610mm");
    public final static MediaSize IsoRa3 = of("iso_ra3_305x430mm");
    public final static MediaSize IsoRa4 = of("iso_ra4_215x305mm");
    public final static MediaSize IsoSra0 = of("iso_sra0_900x1280mm");
    public final static MediaSize IsoSra1 = of("iso_sra1_640x900mm");
    public final static MediaSize IsoSra2 = of("iso_sra2_450x640mm");
    public final static MediaSize IsoSra3 = of("iso_sra3_320x450mm");
    public final static MediaSize IsoSra4 = of("iso_sra4_225x320mm");
    public final static MediaSize JisB0 = of("jis_b0_1030x1456mm");
    public final static MediaSize JisB10 = of("jis_b10_32x45mm");
    public final static MediaSize JisB1 = of("jis_b1_728x1030mm");
    public final static MediaSize JisB2 = of("jis_b2_515x728mm");
    public final static MediaSize JisB3 = of("jis_b3_364x515mm");
    public final static MediaSize JisB4 = of("jis_b4_257x364mm");
    public final static MediaSize JisB5 = of("jis_b5_182x257mm");
    public final static MediaSize JisB6 = of("jis_b6_128x182mm");
    public final static MediaSize JisB7 = of("jis_b7_91x128mm");
    public final static MediaSize JisB8 = of("jis_b8_64x91mm");
    public final static MediaSize JisB9 = of("jis_b9_45x64mm");
    public final static MediaSize JisExec = of("jis_exec_216x330mm");
    public final static MediaSize JpnChou2 = of("jpn_chou2_111.1x146mm");
    public final static MediaSize JpnChou3 = of("jpn_chou3_120x235mm");
    public final static MediaSize JpnChou40 = of("jpn_chou40_90x225mm");
    public final static MediaSize JpnChou4 = of("jpn_chou4_90x205mm");
    public final static MediaSize JpnHagaki = of("jpn_hagaki_100x148mm");
    public final static MediaSize JpnKahu = of("jpn_kahu_240x322.1mm");
    public final static MediaSize JpnKaku2 = of("jpn_kaku2_240x332mm");
    public final static MediaSize JpnKaku3 = of("jpn_kaku3_216x277mm");
    public final static MediaSize JpnKaku4 = of("jpn_kaku4_197x267mm");
    public final static MediaSize JpnKaku5 = of("jpn_kaku5_190x240mm");
    public final static MediaSize JpnKaku7 = of("jpn_kaku7_142x205mm");
    public final static MediaSize JpnKaku8 = of("jpn_kaku8_119x197mm");
    public final static MediaSize JpnOufuku = of("jpn_oufuku_148x200mm");
    public final static MediaSize JpnYou4 = of("jpn_you4_105x235mm");
    public final static MediaSize JpnYou6 = of("jpn_you6_98x190mm");
    public final static MediaSize Na10x11 = of("na_10x11_10x11in");
    public final static MediaSize Na10x13 = of("na_10x13_10x13in");
    public final static MediaSize Na10x14 = of("na_10x14_10x14in");
    public final static MediaSize Na10x15 = of("na_10x15_10x15in");
    public final static MediaSize Na11x12 = of("na_11x12_11x12in");
    public final static MediaSize Na11x15 = of("na_11x15_11x15in");
    public final static MediaSize Na12x19 = of("na_12x19_12x19in");
    public final static MediaSize Na5x7 = of("na_5x7_5x7in");
    public final static MediaSize Na6x9 = of("na_6x9_6x9in");
    public final static MediaSize Na7x9 = of("na_7x9_7x9in");
    public final static MediaSize Na9x11 = of("na_9x11_9x11in");
    public final static MediaSize NaA2 = of("na_a2_4.375x5.75in");
    public final static MediaSize NaArchA = of("na_arch-a_9x12in");
    public final static MediaSize NaArchB = of("na_arch-b_12x18in");
    public final static MediaSize NaArchC = of("na_arch-c_18x24in");
    public final static MediaSize NaArchD = of("na_arch-d_24x36in");
    public final static MediaSize NaArchE = of("na_arch-e_36x48in");
    public final static MediaSize NaBPlus = of("na_b-plus_12x19.17in");
    public final static MediaSize NaC = of("na_c_17x22in");
    public final static MediaSize NaC5 = of("na_c5_6.5x9.5in");
    public final static MediaSize NaD = of("na_d_22x34in");
    public final static MediaSize NaE = of("na_e_34x44in");
    public final static MediaSize NaEdp = of("na_edp_11x14in");
    public final static MediaSize NaEurEdp = of("na_eur-edp_12x14in");
    public final static MediaSize NaExecutive = of("na_executive_7.25x10.5in");
    public final static MediaSize NaF = of("na_f_44x68in");
    public final static MediaSize NaFanfoldEur = of("na_fanfold-eur_8.5x12in");
    public final static MediaSize NaFanfoldUs = of("na_fanfold-us_11x14.875in");
    public final static MediaSize NaFoolscap = of("na_foolscap_8.5x13in");
    public final static MediaSize NaGovtLegal = of("na_govt-legal_8x13in");
    public final static MediaSize NaGovtLetter = of("na_govt-letter_8x10in");
    public final static MediaSize NaIndex3x5 = of("na_index-3x5_3x5in");
    public final static MediaSize NaIndex4x6 = of("na_index-4x6_4x6in");
    public final static MediaSize NaIndex4x6Ext = of("na_index-4x6-ext_6x8in");
    public final static MediaSize NaIndex5x8 = of("na_index-5x8_5x8in");
    public final static MediaSize NaInvoice = of("na_invoice_5.5x8.5in");
    public final static MediaSize NaLedger = of("na_ledger_11x17in");
    public final static MediaSize NaLegal = of("na_legal_8.5x14in");
    public final static MediaSize NaLegalExtra = of("na_legal-extra_9.5x15in");
    public final static MediaSize NaLetter = of("na_letter_8.5x11in");
    public final static MediaSize NaLetterExtra = of("na_letter-extra_9.5x12in");
    public final static MediaSize NaLetterPlus = of("na_letter-plus_8.5x12.69in");
    public final static MediaSize NaMonarch = of("na_monarch_3.875x7.5in");
    public final static MediaSize NaNumber10 = of("na_number-10_4.125x9.5in");
    public final static MediaSize NaNumber11 = of("na_number-11_4.5x10.375in");
    public final static MediaSize NaNumber12 = of("na_number-12_4.75x11in");
    public final static MediaSize NaNumber14 = of("na_number-14_5x11.5in");
    public final static MediaSize NaNumber9 = of("na_number-9_3.875x8.875in");
    public final static MediaSize NaOficio = of("na_oficio_8.5x13.4in");
    public final static MediaSize NaPersonal = of("na_personal_3.625x6.5in");
    public final static MediaSize NaQuarto = of("na_quarto_8.5x10.83in");
    public final static MediaSize NaSuperA = of("na_super-a_8.94x14in");
    public final static MediaSize NaSuperB = of("na_super-b_13x19in");
    public final static MediaSize NaWideFormat = of("na_wide-format_30x42in");
    public final static MediaSize OmDaiPaKai = of("om_dai-pa-kai_275x395mm");
    public final static MediaSize OmFolio = of("om_folio_210x330mm");
    public final static MediaSize OmFolioSp = of("om_folio-sp_215x315mm");
    public final static MediaSize OmInvite = of("om_invite_220x220mm");
    public final static MediaSize OmItalian = of("om_italian_110x230mm");
    public final static MediaSize OmJuuroKuKai = of("om_juuro-ku-kai_198x275mm");
    public final static MediaSize OmLargePhoto = of("om_large-photo_200x300");
    public final static MediaSize OmMediumPhoto = of("om_medium-photo_130x180mm");
    public final static MediaSize OnPaKai = of("om_pa-kai_267x389mm");
    public final static MediaSize OmPostfix = of("om_postfix_114x229mm");
    public final static MediaSize OmSmallPhoto = of("om_small-photo_100x150mm");
    public final static MediaSize OmWidePhoto = of("om_wide-photo_100x200mm");
    public final static MediaSize Prc10 = of("prc_10_324x458mm");
    public final static MediaSize Prc1 = of("prc_1_102x165mm");
    public final static MediaSize Prc16k = of("prc_16k_146x215mm");
    public final static MediaSize Prc2 = of("prc_2_102x176mm");
    public final static MediaSize Prc3 = of("prc_3_125x176mm");
    public final static MediaSize Prc32k = of("prc_32k_97x151mm");
    public final static MediaSize Prc4 = of("prc_4_110x208mm");
    public final static MediaSize Prc6 = of("prc_6_120x320mm");
    public final static MediaSize Prc7 = of("prc_7_160x230mm");
    public final static MediaSize Prc8 = of("prc_8_120x309mm");
    public final static MediaSize Roc16k = of("roc_16k_7.75x10.75in");
    public final static MediaSize Roc8k = of("roc_8k_10.75x15.5in");

    public static MediaSize of(String name, int x, int y) {
        return new AutoValue_MediaSize(name, x, y);
    }

    public static MediaSize of(String name) {

        Matcher matches = WIDTH_HEIGHT.matcher(name);
        if (!matches.find() || matches.groupCount() < 4) {
            throw new IllegalArgumentException("Unparsable Media Size " + name);
        }
        // Look up existing?
        int units;
        if (matches.groupCount() >= 5 && matches.group(5) != null && matches.group(5).equals("in")) {
            units = 2540;
        } else {
            // Assume mm
            units = 100;
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
            String string = StringType.ENCODER.readValue(in, valueTag);
            // TODO
            return null;
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

    public static class Type extends AttributeType<MediaSize> {
        private Type(String name) {
            super(ENCODER, Tag.Keyword, name);
        }
    }

    // Look up / generate a dimension collection
    // Look up / generate a media keyword
}
