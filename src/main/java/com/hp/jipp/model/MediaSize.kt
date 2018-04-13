// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.SimpleEncoder
import com.hp.jipp.encoding.StringType
import com.hp.jipp.encoding.Tag
import com.hp.jipp.util.getStaticObjects

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
@Suppress("LargeClass") // We just have a lot of possible fields
data class MediaSize(val name: String, val width: Int, val height: Int) {

    /** A media size type based solely on keyword values with width/height inferred  */
    open class Type(override val name: String) : AttributeType<MediaSize>(ENCODER, Tag.keyword)

    override fun toString() = "MediaSize($name, ${width}x$height)"
    companion object {
        private val WIDTH_HEIGHT = Pattern.compile(
                "_([0-9]+(\\.[0-9]+)?)?x([0-9]+(\\.[0-9]+)?)([a-z]+)?$")
        private val WIDTH_AT = 1
        private val HEIGHT_AT = 3
        private val WIDTH_HEIGHT_DIMENSION_COUNT = 4
        private val WIDTH_HEIGHT_UNIT_AT = 5

        private val TYPE_NAME = "MediaSize"
        private val MM_HUNDREDTHS_PER_INCH = 2540
        private val MM_HUNDREDTHS_PER_MM = 100

        @JvmField val iso2a0 = of("iso_2a0_1189x1682mm")
        @JvmField val isoA0 = of("iso_a0_841x1189mm")
        @JvmField val isoA0x3 = of("iso_a0x3_1189x2523mm")
        @JvmField val isoA10 = of("iso_a10_26x37mm")
        @JvmField val isoA1 = of("iso_a1_594x841mm")
        @JvmField val isoA1x3 = of("iso_a1x3_841x1783mm")
        @JvmField val isoA1x4 = of("iso_a1x4_841x2378mm")
        @JvmField val isoA2 = of("iso_a2_420x594mm")
        @JvmField val isoA2x3 = of("iso_a2x3_594x1261mm")
        @JvmField val isoA2x4 = of("iso_a2x4_594x1682mm")
        @JvmField val isoA2x5 = of("iso_a2x5_594x2102mm")
        @JvmField val isoA3 = of("iso_a3_297x420mm")
        @JvmField val isoA3Extra = of("iso_a3-extra_322x445mm")
        @JvmField val isoA3x3 = of("iso_a3x3_420x891mm")
        @JvmField val isoA3x4 = of("iso_a3x4_420x1189mm")
        @JvmField val isoA3x5 = of("iso_a3x5_420x1486mm")
        @JvmField val isoA3x6 = of("iso_a3x6_420x1783mm")
        @JvmField val isoA3x7 = of("iso_a3x7_420x2080mm")
        @JvmField val isoA4 = of("iso_a4_210x297mm")
        @JvmField val isoA4Extra = of("iso_a4-extra_235.5x322.3mm")
        @JvmField val isoA4Tab = of("iso_a4-tab_225x297mm")
        @JvmField val isoA4x3 = of("iso_a4x3_297x630mm")
        @JvmField val isoA4x4 = of("iso_a4x4_297x841mm")
        @JvmField val isoA4x5 = of("iso_a4x5_297x1051mm")
        @JvmField val isoA4x6 = of("iso_a4x6_297x1261mm")
        @JvmField val isoA4x7 = of("iso_a4x7_297x1471mm")
        @JvmField val isoA4x8 = of("iso_a4x8_297x1682mm")
        @JvmField val isoA4x9 = of("iso_a4x9_297x1892mm")
        @JvmField val isoA5 = of("iso_a5_148x210mm")
        @JvmField val isoA5Extra = of("iso_a5-extra_174x235mm")
        @JvmField val isoA6 = of("iso_a6_105x148mm")
        @JvmField val isoA7 = of("iso_a7_74x105mm")
        @JvmField val isoA8 = of("iso_a8_52x74mm")
        @JvmField val isoA9 = of("iso_a9_37x52mm")
        @JvmField val isoB0 = of("iso_b0_1000x1414mm")
        @JvmField val isoB10 = of("iso_b10_31x44mm")
        @JvmField val isoB1 = of("iso_b1_707x1000mm")
        @JvmField val isoB2 = of("iso_b2_500x707mm")
        @JvmField val isoB3 = of("iso_b3_353x500mm")
        @JvmField val isoB4 = of("iso_b4_250x353mm")
        @JvmField val isoB5 = of("iso_b5_176x250mm")
        @JvmField val isoB5Extra = of("iso_b5-extra_201x276mm")
        @JvmField val isoB6 = of("iso_b6_125x176mm")
        @JvmField val isoB6c4 = of("iso_b6c4_125x324mm")
        @JvmField val isoB7 = of("iso_b7_88x125mm")
        @JvmField val isoB8 = of("iso_b8_62x88mm")
        @JvmField val isoB9 = of("iso_b9_44x62mm")
        @JvmField val isoC0 = of("iso_c0_917x1297mm")
        @JvmField val isoC10 = of("iso_c10_28x40mm")
        @JvmField val isoC1 = of("iso_c1_648x917mm")
        @JvmField val isoC2 = of("iso_c2_458x648mm")
        @JvmField val isoC3 = of("iso_c3_324x458mm")
        @JvmField val isoC4 = of("iso_c4_229x324mm")
        @JvmField val isoC5 = of("iso_c5_162x229mm")
        @JvmField val isoC6 = of("iso_c6_114x162mm")
        @JvmField val isoC6c5 = of("iso_c6c5_114x229mm")
        @JvmField val isoC7 = of("iso_c7_81x114mm")
        @JvmField val isoC7c6 = of("iso_c7c6_81x162mm")
        @JvmField val isoC8 = of("iso_c8_57x81mm")
        @JvmField val isoC9 = of("iso_c9_40x57mm")
        @JvmField val isoDl = of("iso_dl_110x220mm")
        @JvmField val isoRa0 = of("iso_ra0_860x1220mm")
        @JvmField val isoRa1 = of("iso_ra1_610x860mm")
        @JvmField val isoRa2 = of("iso_ra2_430x610mm")
        @JvmField val isoRa3 = of("iso_ra3_305x430mm")
        @JvmField val isoRa4 = of("iso_ra4_215x305mm")
        @JvmField val isoSra0 = of("iso_sra0_900x1280mm")
        @JvmField val isoSra1 = of("iso_sra1_640x900mm")
        @JvmField val isoSra2 = of("iso_sra2_450x640mm")
        @JvmField val isoSra3 = of("iso_sra3_320x450mm")
        @JvmField val isoSra4 = of("iso_sra4_225x320mm")
        @JvmField val jisB0 = of("jis_b0_1030x1456mm")
        @JvmField val jisB10 = of("jis_b10_32x45mm")
        @JvmField val jisB1 = of("jis_b1_728x1030mm")
        @JvmField val jisB2 = of("jis_b2_515x728mm")
        @JvmField val jisB3 = of("jis_b3_364x515mm")
        @JvmField val jisB4 = of("jis_b4_257x364mm")
        @JvmField val jisB5 = of("jis_b5_182x257mm")
        @JvmField val jisB6 = of("jis_b6_128x182mm")
        @JvmField val jisB7 = of("jis_b7_91x128mm")
        @JvmField val jisB8 = of("jis_b8_64x91mm")
        @JvmField val jisB9 = of("jis_b9_45x64mm")
        @JvmField val jisExec = of("jis_exec_216x330mm")
        @JvmField val jpnChou2 = of("jpn_chou2_111.1x146mm")
        @JvmField val jpnChou3 = of("jpn_chou3_120x235mm")
        @JvmField val jpnChou40 = of("jpn_chou40_90x225mm")
        @JvmField val jpnChou4 = of("jpn_chou4_90x205mm")
        @JvmField val jpnHagaki = of("jpn_hagaki_100x148mm")
        @JvmField val jpnKahu = of("jpn_kahu_240x322.1mm")
        @JvmField val jpnKaku2 = of("jpn_kaku2_240x332mm")
        @JvmField val jpnKaku3 = of("jpn_kaku3_216x277mm")
        @JvmField val jpnKaku4 = of("jpn_kaku4_197x267mm")
        @JvmField val jpnKaku5 = of("jpn_kaku5_190x240mm")
        @JvmField val jpnKaku7 = of("jpn_kaku7_142x205mm")
        @JvmField val jpnKaku8 = of("jpn_kaku8_119x197mm")
        @JvmField val jpnOufuku = of("jpn_oufuku_148x200mm")
        @JvmField val jpnYou4 = of("jpn_you4_105x235mm")
        @JvmField val jpnYou6 = of("jpn_you6_98x190mm")
        @JvmField val na10x11 = of("na_10x11_10x11in")
        @JvmField val na10x13 = of("na_10x13_10x13in")
        @JvmField val na10x14 = of("na_10x14_10x14in")
        @JvmField val na10x15 = of("na_10x15_10x15in")
        @JvmField val na11x12 = of("na_11x12_11x12in")
        @JvmField val na11x15 = of("na_11x15_11x15in")
        @JvmField val na12x19 = of("na_12x19_12x19in")
        @JvmField val na5x7 = of("na_5x7_5x7in")
        @JvmField val na6x9 = of("na_6x9_6x9in")
        @JvmField val na7x9 = of("na_7x9_7x9in")
        @JvmField val na9x11 = of("na_9x11_9x11in")
        @JvmField val naA2 = of("na_a2_4.375x5.75in")
        @JvmField val naArchA = of("na_arch-a_9x12in")
        @JvmField val naArchB = of("na_arch-b_12x18in")
        @JvmField val naArchC = of("na_arch-c_18x24in")
        @JvmField val naArchD = of("na_arch-d_24x36in")
        @JvmField val naArchE = of("na_arch-e_36x48in")
        @JvmField val naBPlus = of("na_b-plus_12x19.17in")
        @JvmField val naC = of("na_c_17x22in")
        @JvmField val naC5 = of("na_c5_6.5x9.5in")
        @JvmField val naD = of("na_d_22x34in")
        @JvmField val naE = of("na_e_34x44in")
        @JvmField val naEdp = of("na_edp_11x14in")
        @JvmField val naEurEdp = of("na_eur-edp_12x14in")
        @JvmField val naExecutive = of("na_executive_7.25x10.5in")
        @JvmField val naF = of("na_f_44x68in")
        @JvmField val naFanfoldEur = of("na_fanfold-eur_8.5x12in")
        @JvmField val naFanfoldUs = of("na_fanfold-us_11x14.875in")
        @JvmField val naFoolscap = of("na_foolscap_8.5x13in")
        @JvmField val naGovtLegal = of("na_govt-legal_8x13in")
        @JvmField val naGovtLetter = of("na_govt-letter_8x10in")
        @JvmField val naIndex3x5 = of("na_index-3x5_3x5in")
        @JvmField val naIndex4x6 = of("na_index-4x6_4x6in")
        @JvmField val naIndex4x6Ext = of("na_index-4x6-ext_6x8in")
        @JvmField val naIndex5x8 = of("na_index-5x8_5x8in")
        @JvmField val naInvoice = of("na_invoice_5.5x8.5in")
        @JvmField val naLedger = of("na_ledger_11x17in")
        @JvmField val naLegal = of("na_legal_8.5x14in")
        @JvmField val naLegalExtra = of("na_legal-extra_9.5x15in")
        @JvmField val naLetter = of("na_letter_8.5x11in")
        @JvmField val naLetterExtra = of("na_letter-extra_9.5x12in")
        @JvmField val naLetterPlus = of("na_letter-plus_8.5x12.69in")
        @JvmField val naMonarch = of("na_monarch_3.875x7.5in")
        @JvmField val naNumber10 = of("na_number-10_4.125x9.5in")
        @JvmField val naNumber11 = of("na_number-11_4.5x10.375in")
        @JvmField val naNumber12 = of("na_number-12_4.75x11in")
        @JvmField val naNumber14 = of("na_number-14_5x11.5in")
        @JvmField val naNumber9 = of("na_number-9_3.875x8.875in")
        @JvmField val naOficio = of("na_oficio_8.5x13.4in")
        @JvmField val naPersonal = of("na_personal_3.625x6.5in")
        @JvmField val naQuarto = of("na_quarto_8.5x10.83in")
        @JvmField val naSuperA = of("na_super-a_8.94x14in")
        @JvmField val naSuperB = of("na_super-b_13x19in")
        @JvmField val naWideFormat = of("na_wide-format_30x42in")
        @JvmField val omDaiPaKai = of("om_dai-pa-kai_275x395mm")
        @JvmField val omFolio = of("om_folio_210x330mm")
        @JvmField val omFolioSp = of("om_folio-sp_215x315mm")
        @JvmField val omInvite = of("om_invite_220x220mm")
        @JvmField val omItalian = of("om_italian_110x230mm")
        @JvmField val omJuuroKuKai = of("om_juuro-ku-kai_198x275mm")
        @JvmField val omLargePhoto = of("om_large-photo_200x300")
        @JvmField val omMediumPhoto = of("om_medium-photo_130x180mm")
        @JvmField val onPaKai = of("om_pa-kai_267x389mm")
        @JvmField val omPostfix = of("om_postfix_114x229mm")
        @JvmField val omSmallPhoto = of("om_small-photo_100x150mm")
        @JvmField val omWidePhoto = of("om_wide-photo_100x200mm")
        @JvmField val prc10 = of("prc_10_324x458mm")
        @JvmField val prc1 = of("prc_1_102x165mm")
        @JvmField val prc16k = of("prc_16k_146x215mm")
        @JvmField val prc2 = of("prc_2_102x176mm")
        @JvmField val prc3 = of("prc_3_125x176mm")
        @JvmField val prc32k = of("prc_32k_97x151mm")
        @JvmField val prc4 = of("prc_4_110x208mm")
        @JvmField val prc6 = of("prc_6_120x320mm")
        @JvmField val prc7 = of("prc_7_160x230mm")
        @JvmField val prc8 = of("prc_8_120x309mm")
        @JvmField val roc16k = of("roc_16k_7.75x10.75in")
        @JvmField val roc8k = of("roc_8k_10.75x15.5in")

        private val all: Map<String, MediaSize> = MediaSize::class.java.getStaticObjects()
                .filter { MediaSize::class.java.isAssignableFrom(it.javaClass) }
                .map { (it as MediaSize).name to it }.toMap()

        @JvmStatic
        fun of(name: String): MediaSize {
            val matches = WIDTH_HEIGHT.matcher(name)
            if (!matches.find() || matches.groupCount() < WIDTH_HEIGHT_DIMENSION_COUNT) {
                // No way to guess media size from name
                return MediaSize(name, 0, 0)
            }

            // Assume mm unless "in"
            val unitString = if (matches.groupCount() >= WIDTH_HEIGHT_UNIT_AT) {
                matches.group(WIDTH_HEIGHT_UNIT_AT)
            } else {
                null
            }
            val units = if (unitString == "in") MM_HUNDREDTHS_PER_INCH else MM_HUNDREDTHS_PER_MM
            val x = (java.lang.Double.parseDouble(matches.group(WIDTH_AT)) * units).toInt()
            val y = (java.lang.Double.parseDouble(matches.group(HEIGHT_AT)) * units).toInt()
            return MediaSize(name, x, y)
        }

        @JvmField val ENCODER: SimpleEncoder<MediaSize> = object : SimpleEncoder<MediaSize>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag): MediaSize {
                val name = StringType.Encoder.readValue(input, valueTag)
                return all[name] ?: MediaSize.of(name)
            }

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: MediaSize) {
                StringType.Encoder.writeValue(out, value.name)
            }

            override fun valid(valueTag: Tag): Boolean {
                return Tag.keyword == valueTag || Tag.nameWithoutLanguage == valueTag
            }
        }
    }
}
