package momento.ifrn.meal.post.infrastructure.image;

import jakarta.annotation.PostConstruct;
import momento.ifrn.meal.post.core.infrastructure.ImageGeneratorService;
import momento.ifrn.meal.post.domain.DinnerMenu;
import momento.ifrn.meal.post.domain.enums.MealType;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AwtImageGeneratorService implements ImageGeneratorService {

    private static final float FONT_SIZE = 47.4f;
    private static final String FONT_PATH = "/fonts/OpenSans-Regular.ttf";
    private static final Color TEXT_COLOR = Color.BLACK;

    private static final int BOX_X = 170;
    private static final int BOX_WIDTH = 740;


    private static final int TEXT_START_Y = 690;

    private static final int LINE_SPACING = 10;
    private static final int ITEM_SPACING = 18;
    private static final String BULLET = "•  ";

    private Font openSansFont;

    @PostConstruct
    void loadFont() throws FontFormatException, IOException {
        try (InputStream is = getClass().getResourceAsStream(FONT_PATH)) {
            if (is == null) {
                throw new IllegalStateException("Fonte não encontrada em " + FONT_PATH);
            }
            final Font base = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
            this.openSansFont = base.deriveFont(FONT_SIZE);
        }
    }

    @Override
    public byte[] generate(final DinnerMenu menu) {
        final BufferedImage template = loadTemplate(menu.getMealType());
        final BufferedImage result = new BufferedImage(
                template.getWidth(), template.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g = result.createGraphics();
        try {
            applyQualityHints(g);
            g.drawImage(template, 0, 0, null);
            g.setFont(openSansFont);
            g.setColor(TEXT_COLOR);
            drawItems(g, menu.getText());
        } finally {
            g.dispose();
        }

        return toPngBytes(result);
    }

    private BufferedImage loadTemplate(final MealType mealType) {
        final String fileName = switch (mealType) {
            case SNACK -> "lanche.png";
            case LUNCH -> "almoco.png";
            case DINNER -> "janta.png";
        };
        try (InputStream is = getClass().getResourceAsStream("/static/" + fileName)) {
            if (is == null) {
                throw new IllegalStateException("Template não encontrado: " + fileName);
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao carregar template " + fileName, e);
        }
    }

    private void applyQualityHints(final Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    private List<String> parseItems(final String rawText) {
        return Arrays.stream(rawText.split("[\\n,]+"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private void drawItems(final Graphics2D g, final String rawText) {
        final FontMetrics metrics = g.getFontMetrics();
        final List<String> items = parseItems(rawText);

        final int lineHeight = metrics.getAscent() + metrics.getDescent() + LINE_SPACING;
        final int bulletWidth = metrics.stringWidth(BULLET);

        int y = TEXT_START_Y + metrics.getAscent();

        for (final String item : items) {
            final List<String> wrappedLines = wrapText(item, metrics, BOX_WIDTH - bulletWidth);

            for (int i = 0; i < wrappedLines.size(); i++) {
                final String line = wrappedLines.get(i);

                if (i == 0) {
                    g.drawString(BULLET + line, BOX_X, y);
                } else {
                    g.drawString(line, BOX_X + bulletWidth, y);
                }

                y += lineHeight;
            }

            y += ITEM_SPACING;
        }
    }

    private List<String> wrapText(final String text, final FontMetrics metrics, final int maxWidth) {
        final List<String> lines = new ArrayList<>();
        final StringBuilder currentLine = new StringBuilder();

        for (final String word : text.split(" ")) {
            final String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;

            if (metrics.stringWidth(candidate) > maxWidth && !currentLine.isEmpty()) {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentLine.append(word);
            } else {
                currentLine.setLength(0);
                currentLine.append(candidate);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private byte[] toPngBytes(final BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao converter imagem para PNG", e);
        }
    }
}