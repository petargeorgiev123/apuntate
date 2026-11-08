import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Genera el icono (512x512) y la imagen destacada (1024x500) para la ficha de Google Play. */
public class GenerarGraficos {

    static Font fuente(float tam, int estilo) throws Exception {
        File f = new File("/usr/share/fonts/google-noto-vf/NotoSans[wght].ttf");
        return Font.createFont(Font.TRUETYPE_FONT, f).deriveFont(estilo, tam);
    }

    static Graphics2D g2(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }

    static void centrado(Graphics2D g, String texto, int cx, int baseY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, cx - fm.stringWidth(texto) / 2, baseY);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        // ---- Icono 512x512 (cuadrado completo; Play aplica la máscara) ----
        BufferedImage icono = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = g2(icono);
        g.setColor(new Color(0x15, 0x65, 0xC0));
        g.fillRect(0, 0, 512, 512);
        g.setColor(Color.WHITE);
        g.setFont(fuente(360, Font.BOLD));
        FontMetrics fm = g.getFontMetrics();
        centrado(g, "\u20AC", 256, 256 + (fm.getAscent() - fm.getDescent()) / 2);
        g.dispose();
        ImageIO.write(icono, "png", new File("icono-512.png"));

        // ---- Imagen destacada 1024x500 ----
        BufferedImage ft = new BufferedImage(1024, 500, BufferedImage.TYPE_INT_RGB);
        g = g2(ft);
        g.setPaint(new GradientPaint(0, 0, new Color(0x2D, 0x37, 0x48), 1024, 500, new Color(0x4A, 0x55, 0x68)));
        g.fillRect(0, 0, 1024, 500);

        // Icono redondeado a la izquierda
        g.setColor(new Color(0x15, 0x65, 0xC0));
        g.fill(new RoundRectangle2D.Float(90, 150, 200, 200, 48, 48));
        g.setColor(Color.WHITE);
        g.setFont(fuente(150, Font.BOLD));
        fm = g.getFontMetrics();
        centrado(g, "\u20AC", 190, 250 + (fm.getAscent() - fm.getDescent()) / 2);

        // Textos
        g.setColor(Color.WHITE);
        g.setFont(fuente(96, Font.BOLD));
        g.drawString("Apuntate", 340, 252);
        g.setColor(new Color(0xE2, 0xE8, 0xF0));
        g.setFont(fuente(36, Font.PLAIN));
        g.drawString("Reparte los gastos del grupo", 344, 312);
        g.drawString("y chatea con tu equipo", 344, 358);
        g.dispose();
        ImageIO.write(ft, "png", new File("imagen-destacada-1024x500.png"));

        System.out.println("OK");
    }
}
