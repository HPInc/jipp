package sample.jrender;

import com.hp.jipp.model.MediaSource;
import com.hp.jipp.model.Sides;
import com.hp.jipp.pdl.ColorSpace;
import com.hp.jipp.pdl.OutputSettings;
import com.hp.jipp.pdl.RenderableDocument;
import com.hp.jipp.pdl.RenderablePage;
import com.hp.jipp.pdl.pclm.PclmSettings;
import com.hp.jipp.pdl.pclm.PclmWriter;
import com.hp.jipp.pdl.pwg.PwgSettings;
import com.hp.jipp.pdl.pwg.PwgWriter;
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
import java.util.Iterator;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

class Main {
    private static final int DPI = 300;
    private static final ImageType IMAGE_TYPE = ImageType.RGB;
    private final static double RED_COEFFICIENT = 0.2126;
    private final static double GREEN_COEFFICIENT = 0.7512;
    private final static double BLUE_COEFFICIENT = 0.0722;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Arguments [PDF_FILE] [OUT_FILE] are required, received: " +
                    Arrays.asList(args));
        }

        OutputFormat outputFormat = OutputFormat.toOutputFormat(getExtension(args[1]));

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

                        int byteIndex = 0;
                        for (int y = yOffset; y < (yOffset + swathHeight); y++) {
                            for (int x = 0; x < width; x++) {

                                rgb = image.getRGB(x, y);
                                red = (rgb >> 16) & 0xFF;
                                green = (rgb >> 8) & 0xFF;
                                blue = rgb & 0xFF;

                                if (colorSpace == ColorSpace.Grayscale) {
                                    byteArray[byteIndex++] = (byte) (RED_COEFFICIENT * red +
                                            GREEN_COEFFICIENT * green + BLUE_COEFFICIENT * blue);
                                } else {
                                    byteArray[byteIndex++] = (byte) red;
                                    byteArray[byteIndex++] = (byte) green;
                                    byteArray[byteIndex++] = (byte) blue;
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

            switch (outputFormat) {
                case PCLM:
                    saveRenderableDocumentAsPCLm(renderableDocument, colorSpace, outputStream);
                    break;
                case PWG_RASTER:
                    saveRenderableDocumentAsPWG(renderableDocument, colorSpace, outputStream);
                    break;
            }
        }
    }

    private static String getExtension(String name) {
        int index = name.lastIndexOf(".");
        if (index == -1 || index <= name.lastIndexOf("/")) {
            throw new IllegalArgumentException(name + " has no extension");
        }
        return name.substring(index + 1);
    }

    private static void saveRenderableDocumentAsPCLm(RenderableDocument renderableDocument,
                                                     ColorSpace colorSpace, OutputStream outputStream) throws IOException {

        OutputSettings outputSettings = new OutputSettings(colorSpace, Sides.oneSided, MediaSource.auto, null, false);
        PclmSettings caps = new PclmSettings(outputSettings, 64);

        PclmWriter writer = new PclmWriter(outputStream, caps);
        writer.write(renderableDocument);
        writer.close();

    }

    private static void saveRenderableDocumentAsPWG(RenderableDocument renderableDocument,
                                                    ColorSpace colorSpace, OutputStream outputStream) throws IOException {

        OutputSettings outputSettings = new OutputSettings(colorSpace, Sides.oneSided, MediaSource.auto, null, false);
        PwgSettings caps = new PwgSettings(outputSettings);

        PwgWriter writer = new PwgWriter(outputStream, caps);
        writer.write(renderableDocument);
        writer.close();
    }

    private static ColorSpace convertImageTypeToColorSpace(ImageType imageType) {
        switch (imageType) {
            case BINARY:
            case GRAY:
                return ColorSpace.Grayscale;
            default:
                return ColorSpace.Rgb;
        }
    }

    public enum OutputFormat {
        PWG_RASTER("pwg"),
        PCLM("PCLm");

        private final String name;
        OutputFormat(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }

        public static OutputFormat toOutputFormat(String formatName) {
            for (OutputFormat format : OutputFormat.values()) {
                if (format.getName().equalsIgnoreCase(formatName)) {
                    return format;
                }
            }
            throw new IllegalArgumentException("Output format " + formatName + " is invalid");
        }
    }
}
