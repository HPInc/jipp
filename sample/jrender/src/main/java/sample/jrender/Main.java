package sample.jrender;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.hp.jipp.model.MediaSource;
import com.hp.jipp.model.PwgRasterDocumentSheetBack;
import com.hp.jipp.model.Sides;
import com.hp.jipp.pdl.ColorSpace;
import com.hp.jipp.pdl.OutputSettings;
import com.hp.jipp.pdl.RenderableDocument;
import com.hp.jipp.pdl.RenderablePage;
import com.hp.jipp.pdl.pclm.PclmSettings;
import com.hp.jipp.pdl.pclm.PclmWriter;
import com.hp.jipp.pdl.pwg.PwgSettings;
import com.hp.jipp.pdl.pwg.PwgWriter;

class Main {

    private static final int DPI = 300;
    private static final ImageType IMAGE_TYPE = ImageType.RGB;

    private static final int BLACK_LIMIT = 380;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Arguments [PDF_FILE] [OUT_FILE] are required, received: " +
                    Arrays.asList(args));
        }

        OutputFormat outputFormat = OutputFormat.PWG_RASTER;
        if (args.length >= 3) {
            outputFormat = OutputFormat.toOutputFormat(args[2]);
        }

        InputStream pdfInputStream = new BufferedInputStream(new FileInputStream(new File(args[0])));
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(args[1])));

        ColorSpace colorSpace = convertImageTypeToColorSpace(IMAGE_TYPE);

        try (PDDocument document = PDDocument.load(pdfInputStream)) {

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            PDPageTree pages = document.getPages();

            List<RenderablePage> renderablePages = new ArrayList<>();

            for (int pageIndex = 0; pageIndex < pages.getCount(); pageIndex++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, DPI, IMAGE_TYPE);

                int width = image.getWidth();
                int height = image.getHeight();

                RenderablePage renderablePage = new RenderablePage(width, height) {

                    @Override
                    public void render(int yOffset, int swathHeight, ColorSpace colorSpace, byte[] byteArray) {

                        int red, green, blue, rgb;
                        byte b;

                        int byteIndex = 0;
                        for (int y = yOffset; y < (yOffset + swathHeight); y++) {
                            for (int x = 0; x < width; x++) {

                                rgb = image.getRGB(x, y);

                                red = ((rgb >> 16) & 0xFF);
                                green = ((rgb >> 8) & 0xFF);
                                blue = ((rgb) & 0xFF);

                                if (colorSpace == ColorSpace.Grayscale) {
                                    b = 0x00;
                                    int totalColor = red + green + blue;
                                    if (totalColor > BLACK_LIMIT) {
                                        b = (byte) 0xff;
                                    }
                                    byteArray[byteIndex] = b;
                                    byteIndex++;
                                } else {
                                    byteArray[byteIndex] = (byte) red;
                                    byteIndex++;
                                    byteArray[byteIndex] = (byte) green;
                                    byteIndex++;
                                    byteArray[byteIndex] = (byte) blue;
                                    byteIndex++;
                                }
                            }
                        }
                    }
                };
                renderablePages.add(renderablePage);
            }

            RenderableDocument renderableDocument = new RenderableDocument() {

                @Override
                public Iterator<RenderablePage> iterator() {
                    return renderablePages.iterator();
                }

                @Override
                public int getDpi() {
                    return DPI;
                }
            };

            if (outputFormat == OutputFormat.PCLM) {
                saveRenderableDocumentAsPCLm(renderableDocument, colorSpace, outputStream);
            } else {
                saveRenderableDocumentAsPWG(renderableDocument, colorSpace, outputStream);
            }
        }
    }

    private static void saveRenderableDocumentAsPCLm(RenderableDocument renderableDocument,
                                                     ColorSpace colorSpace, OutputStream outputStream) throws IOException {

        OutputSettings outputSettings = new OutputSettings(colorSpace, Sides.oneSided, MediaSource.auto, null, false);
        PclmSettings caps = new PclmSettings(outputSettings, 64, PwgRasterDocumentSheetBack.normal);

        PclmWriter writer = new PclmWriter(outputStream, caps);
        writer.write(renderableDocument);
        writer.flush();
        writer.close();

    }

    private static void saveRenderableDocumentAsPWG(RenderableDocument renderableDocument,
                                                    ColorSpace colorSpace, OutputStream outputStream) throws IOException {

        OutputSettings outputSettings = new OutputSettings(colorSpace, Sides.oneSided, MediaSource.auto, null, false);
        PwgSettings caps = new PwgSettings(outputSettings, PwgRasterDocumentSheetBack.normal);

        PwgWriter writer = new PwgWriter(outputStream, caps);
        writer.write(renderableDocument);
        writer.flush();
        writer.close();
    }

    private static ColorSpace convertImageTypeToColorSpace(ImageType imageType) {
        switch (imageType) {
            case BINARY :
            case GRAY : {
                return ColorSpace.Grayscale;
            }
            default :
                return ColorSpace.Rgb;
        }
    }

    public enum OutputFormat {
        PWG_RASTER("pwg"),
        PCLM("PCLm");

        private final String name;
        private OutputFormat(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }

        public static OutputFormat toOutputFormat(String formatName) {
            for (OutputFormat format :EnumSet.allOf(OutputFormat.class)) {
                if (format.getName().equalsIgnoreCase(formatName)) {
                    return format;
                }
            }
            throw new IllegalArgumentException("Output Format " + formatName + " is invalid");
        }
    }
}