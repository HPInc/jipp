package com.hp.jipp.model

import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.SimpleEncoder
import com.hp.jipp.encoding.StringType
import com.hp.jipp.encoding.Tag
import com.hp.jipp.util.Util

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.regex.Pattern

/**
 * @param name Self-Describing name as per
 * [PWG5101.1](https://ftp.pwg.org/pub/pwg/candidates/cs-pwgmsn20-20130328-5101.1.pdf)
 * @param width in 1/100 of a millimeter or 1/2540 of an inch
 * @param height in 1/100 of a millimeter or 1/2540 of an inch
 */
data class MediaSize(val name: String, val width: Int, val height: Int) {

    /** A media size type based solely on keyword values with width/height inferred  */
    class Type(name: String) : AttributeType<MediaSize>(ENCODER, Tag.Keyword, name)

    companion object {
        private val WIDTH_HEIGHT = Pattern.compile(
                "_([0-9]+(\\.[0-9]+)?)?x([0-9]+(\\.[0-9]+)?)([a-z]+)?$")
        private val TYPE_NAME = "MediaSize"
        private val MM_HUNDREDTHS_PER_INCH = 2540
        private val MM_HUNDREDTHS_PER_MM = 100

        @JvmField val Iso2a0 = of("iso_2a0_1189x1682mm")
        @JvmField val IsoA0 = of("iso_a0_841x1189mm")
        @JvmField val IsoA0x3 = of("iso_a0x3_1189x2523mm")
        @JvmField val IsoA10 = of("iso_a10_26x37mm")
        @JvmField val IsoA1 = of("iso_a1_594x841mm")
        @JvmField val IsoA1x3 = of("iso_a1x3_841x1783mm")
        @JvmField val IsoA1x4 = of("iso_a1x4_841x2378mm")
        @JvmField val IsoA2 = of("iso_a2_420x594mm")
        @JvmField val IsoA2x3 = of("iso_a2x3_594x1261mm")
        @JvmField val IsoA2x4 = of("iso_a2x4_594x1682mm")
        @JvmField val IsoA2x5 = of("iso_a2x5_594x2102mm")
        @JvmField val IsoA3 = of("iso_a3_297x420mm")
        @JvmField val IsoA3Extra = of("iso_a3-extra_322x445mm")
        @JvmField val IsoA3x3 = of("iso_a3x3_420x891mm")
        @JvmField val IsoA3x4 = of("iso_a3x4_420x1189mm")
        @JvmField val IsoA3x5 = of("iso_a3x5_420x1486mm")
        @JvmField val IsoA3x6 = of("iso_a3x6_420x1783mm")
        @JvmField val IsoA3x7 = of("iso_a3x7_420x2080mm")
        @JvmField val IsoA4 = of("iso_a4_210x297mm")
        @JvmField val IsoA4Extra = of("iso_a4-extra_235.5x322.3mm")
        @JvmField val IsoA4Tab = of("iso_a4-tab_225x297mm")
        @JvmField val IsoA4x3 = of("iso_a4x3_297x630mm")
        @JvmField val IsoA4x4 = of("iso_a4x4_297x841mm")
        @JvmField val IsoA4x5 = of("iso_a4x5_297x1051mm")
        @JvmField val IsoA4x6 = of("iso_a4x6_297x1261mm")
        @JvmField val IsoA4x7 = of("iso_a4x7_297x1471mm")
        @JvmField val IsoA4x8 = of("iso_a4x8_297x1682mm")
        @JvmField val IsoA4x9 = of("iso_a4x9_297x1892mm")
        @JvmField val IsoA5 = of("iso_a5_148x210mm")
        @JvmField val IsoA5Extra = of("iso_a5-extra_174x235mm")
        @JvmField val IsoA6 = of("iso_a6_105x148mm")
        @JvmField val IsoA7 = of("iso_a7_74x105mm")
        @JvmField val IsoA8 = of("iso_a8_52x74mm")
        @JvmField val IsoA9 = of("iso_a9_37x52mm")
        @JvmField val IsoB0 = of("iso_b0_1000x1414mm")
        @JvmField val IsoB10 = of("iso_b10_31x44mm")
        @JvmField val IsoB1 = of("iso_b1_707x1000mm")
        @JvmField val IsoB2 = of("iso_b2_500x707mm")
        @JvmField val IsoB3 = of("iso_b3_353x500mm")
        @JvmField val IsoB4 = of("iso_b4_250x353mm")
        @JvmField val IsoB5 = of("iso_b5_176x250mm")
        @JvmField val IsoB5Extra = of("iso_b5-extra_201x276mm")
        @JvmField val IsoB6 = of("iso_b6_125x176mm")
        @JvmField val IsoB6c4 = of("iso_b6c4_125x324mm")
        @JvmField val IsoB7 = of("iso_b7_88x125mm")
        @JvmField val IsoB8 = of("iso_b8_62x88mm")
        @JvmField val IsoB9 = of("iso_b9_44x62mm")
        @JvmField val IsoC0 = of("iso_c0_917x1297mm")
        @JvmField val IsoC10 = of("iso_c10_28x40mm")
        @JvmField val IsoC1 = of("iso_c1_648x917mm")
        @JvmField val IsoC2 = of("iso_c2_458x648mm")
        @JvmField val IsoC3 = of("iso_c3_324x458mm")
        @JvmField val IsoC4 = of("iso_c4_229x324mm")
        @JvmField val IsoC5 = of("iso_c5_162x229mm")
        @JvmField val IsoC6 = of("iso_c6_114x162mm")
        @JvmField val IsoC6c5 = of("iso_c6c5_114x229mm")
        @JvmField val IsoC7 = of("iso_c7_81x114mm")
        @JvmField val IsoC7c6 = of("iso_c7c6_81x162mm")
        @JvmField val IsoC8 = of("iso_c8_57x81mm")
        @JvmField val IsoC9 = of("iso_c9_40x57mm")
        @JvmField val IsoDl = of("iso_dl_110x220mm")
        @JvmField val IsoRa0 = of("iso_ra0_860x1220mm")
        @JvmField val IsoRa1 = of("iso_ra1_610x860mm")
        @JvmField val IsoRa2 = of("iso_ra2_430x610mm")
        @JvmField val IsoRa3 = of("iso_ra3_305x430mm")
        @JvmField val IsoRa4 = of("iso_ra4_215x305mm")
        @JvmField val IsoSra0 = of("iso_sra0_900x1280mm")
        @JvmField val IsoSra1 = of("iso_sra1_640x900mm")
        @JvmField val IsoSra2 = of("iso_sra2_450x640mm")
        @JvmField val IsoSra3 = of("iso_sra3_320x450mm")
        @JvmField val IsoSra4 = of("iso_sra4_225x320mm")
        @JvmField val JisB0 = of("jis_b0_1030x1456mm")
        @JvmField val JisB10 = of("jis_b10_32x45mm")
        @JvmField val JisB1 = of("jis_b1_728x1030mm")
        @JvmField val JisB2 = of("jis_b2_515x728mm")
        @JvmField val JisB3 = of("jis_b3_364x515mm")
        @JvmField val JisB4 = of("jis_b4_257x364mm")
        @JvmField val JisB5 = of("jis_b5_182x257mm")
        @JvmField val JisB6 = of("jis_b6_128x182mm")
        @JvmField val JisB7 = of("jis_b7_91x128mm")
        @JvmField val JisB8 = of("jis_b8_64x91mm")
        @JvmField val JisB9 = of("jis_b9_45x64mm")
        @JvmField val JisExec = of("jis_exec_216x330mm")
        @JvmField val JpnChou2 = of("jpn_chou2_111.1x146mm")
        @JvmField val JpnChou3 = of("jpn_chou3_120x235mm")
        @JvmField val JpnChou40 = of("jpn_chou40_90x225mm")
        @JvmField val JpnChou4 = of("jpn_chou4_90x205mm")
        @JvmField val JpnHagaki = of("jpn_hagaki_100x148mm")
        @JvmField val JpnKahu = of("jpn_kahu_240x322.1mm")
        @JvmField val JpnKaku2 = of("jpn_kaku2_240x332mm")
        @JvmField val JpnKaku3 = of("jpn_kaku3_216x277mm")
        @JvmField val JpnKaku4 = of("jpn_kaku4_197x267mm")
        @JvmField val JpnKaku5 = of("jpn_kaku5_190x240mm")
        @JvmField val JpnKaku7 = of("jpn_kaku7_142x205mm")
        @JvmField val JpnKaku8 = of("jpn_kaku8_119x197mm")
        @JvmField val JpnOufuku = of("jpn_oufuku_148x200mm")
        @JvmField val JpnYou4 = of("jpn_you4_105x235mm")
        @JvmField val JpnYou6 = of("jpn_you6_98x190mm")
        @JvmField val Na10x11 = of("na_10x11_10x11in")
        @JvmField val Na10x13 = of("na_10x13_10x13in")
        @JvmField val Na10x14 = of("na_10x14_10x14in")
        @JvmField val Na10x15 = of("na_10x15_10x15in")
        @JvmField val Na11x12 = of("na_11x12_11x12in")
        @JvmField val Na11x15 = of("na_11x15_11x15in")
        @JvmField val Na12x19 = of("na_12x19_12x19in")
        @JvmField val Na5x7 = of("na_5x7_5x7in")
        @JvmField val Na6x9 = of("na_6x9_6x9in")
        @JvmField val Na7x9 = of("na_7x9_7x9in")
        @JvmField val Na9x11 = of("na_9x11_9x11in")
        @JvmField val NaA2 = of("na_a2_4.375x5.75in")
        @JvmField val NaArchA = of("na_arch-a_9x12in")
        @JvmField val NaArchB = of("na_arch-b_12x18in")
        @JvmField val NaArchC = of("na_arch-c_18x24in")
        @JvmField val NaArchD = of("na_arch-d_24x36in")
        @JvmField val NaArchE = of("na_arch-e_36x48in")
        @JvmField val NaBPlus = of("na_b-plus_12x19.17in")
        @JvmField val NaC = of("na_c_17x22in")
        @JvmField val NaC5 = of("na_c5_6.5x9.5in")
        @JvmField val NaD = of("na_d_22x34in")
        @JvmField val NaE = of("na_e_34x44in")
        @JvmField val NaEdp = of("na_edp_11x14in")
        @JvmField val NaEurEdp = of("na_eur-edp_12x14in")
        @JvmField val NaExecutive = of("na_executive_7.25x10.5in")
        @JvmField val NaF = of("na_f_44x68in")
        @JvmField val NaFanfoldEur = of("na_fanfold-eur_8.5x12in")
        @JvmField val NaFanfoldUs = of("na_fanfold-us_11x14.875in")
        @JvmField val NaFoolscap = of("na_foolscap_8.5x13in")
        @JvmField val NaGovtLegal = of("na_govt-legal_8x13in")
        @JvmField val NaGovtLetter = of("na_govt-letter_8x10in")
        @JvmField val NaIndex3x5 = of("na_index-3x5_3x5in")
        @JvmField val NaIndex4x6 = of("na_index-4x6_4x6in")
        @JvmField val NaIndex4x6Ext = of("na_index-4x6-ext_6x8in")
        @JvmField val NaIndex5x8 = of("na_index-5x8_5x8in")
        @JvmField val NaInvoice = of("na_invoice_5.5x8.5in")
        @JvmField val NaLedger = of("na_ledger_11x17in")
        @JvmField val NaLegal = of("na_legal_8.5x14in")
        @JvmField val NaLegalExtra = of("na_legal-extra_9.5x15in")
        @JvmField val NaLetter = of("na_letter_8.5x11in")
        @JvmField val NaLetterExtra = of("na_letter-extra_9.5x12in")
        @JvmField val NaLetterPlus = of("na_letter-plus_8.5x12.69in")
        @JvmField val NaMonarch = of("na_monarch_3.875x7.5in")
        @JvmField val NaNumber10 = of("na_number-10_4.125x9.5in")
        @JvmField val NaNumber11 = of("na_number-11_4.5x10.375in")
        @JvmField val NaNumber12 = of("na_number-12_4.75x11in")
        @JvmField val NaNumber14 = of("na_number-14_5x11.5in")
        @JvmField val NaNumber9 = of("na_number-9_3.875x8.875in")
        @JvmField val NaOficio = of("na_oficio_8.5x13.4in")
        @JvmField val NaPersonal = of("na_personal_3.625x6.5in")
        @JvmField val NaQuarto = of("na_quarto_8.5x10.83in")
        @JvmField val NaSuperA = of("na_super-a_8.94x14in")
        @JvmField val NaSuperB = of("na_super-b_13x19in")
        @JvmField val NaWideFormat = of("na_wide-format_30x42in")
        @JvmField val OmDaiPaKai = of("om_dai-pa-kai_275x395mm")
        @JvmField val OmFolio = of("om_folio_210x330mm")
        @JvmField val OmFolioSp = of("om_folio-sp_215x315mm")
        @JvmField val OmInvite = of("om_invite_220x220mm")
        @JvmField val OmItalian = of("om_italian_110x230mm")
        @JvmField val OmJuuroKuKai = of("om_juuro-ku-kai_198x275mm")
        @JvmField val OmLargePhoto = of("om_large-photo_200x300")
        @JvmField val OmMediumPhoto = of("om_medium-photo_130x180mm")
        @JvmField val OnPaKai = of("om_pa-kai_267x389mm")
        @JvmField val OmPostfix = of("om_postfix_114x229mm")
        @JvmField val OmSmallPhoto = of("om_small-photo_100x150mm")
        @JvmField val OmWidePhoto = of("om_wide-photo_100x200mm")
        @JvmField val Prc10 = of("prc_10_324x458mm")
        @JvmField val Prc1 = of("prc_1_102x165mm")
        @JvmField val Prc16k = of("prc_16k_146x215mm")
        @JvmField val Prc2 = of("prc_2_102x176mm")
        @JvmField val Prc3 = of("prc_3_125x176mm")
        @JvmField val Prc32k = of("prc_32k_97x151mm")
        @JvmField val Prc4 = of("prc_4_110x208mm")
        @JvmField val Prc6 = of("prc_6_120x320mm")
        @JvmField val Prc7 = of("prc_7_160x230mm")
        @JvmField val Prc8 = of("prc_8_120x309mm")
        @JvmField val Roc16k = of("roc_16k_7.75x10.75in")
        @JvmField val Roc8k = of("roc_8k_10.75x15.5in")

        private val all: Map<String, MediaSize> = Util.getStaticObjects(MediaSize::class.java)
                .filter { MediaSize::class.java.isAssignableFrom(it.javaClass) }
                .map { (it as MediaSize).name to it }.toMap()

        private fun of(name: String): MediaSize {
            val matches = WIDTH_HEIGHT.matcher(name)
            if (!matches.find() || matches.groupCount() < 4) {
                // No way to guess media size from name
                return MediaSize(name, 0, 0)
            }

            // Assume mm
            var units = MM_HUNDREDTHS_PER_MM
            if (matches.groupCount() >= 5 && matches.group(5) != null && matches.group(5) == "in") {
                units = MM_HUNDREDTHS_PER_INCH
            }

            val x = (java.lang.Double.parseDouble(matches.group(1)) * units).toInt()
            val y = (java.lang.Double.parseDouble(matches.group(3)) * units).toInt()
            return MediaSize(name, x, y)
        }

        @JvmField val ENCODER: SimpleEncoder<MediaSize> = object : SimpleEncoder<MediaSize>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag): MediaSize {
                val name = StringType.ENCODER.readValue(input, valueTag)
                return all[name] ?: MediaSize.of(name)
            }

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: MediaSize) {
                StringType.ENCODER.writeValue(out, value.name)
            }

            override fun valid(valueTag: Tag): Boolean {
                return Tag.Keyword == valueTag || Tag.NameWithoutLanguage == valueTag
            }
        }
    }
}
