package cn.misakanet;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class UIConsoleOutputStream extends ByteArrayOutputStream {
    private final PrintStream console;

    private final JTextPane target;
    private final StyledDocument doc;
    private final SimpleAttributeSet attr;

    public UIConsoleOutputStream(JTextPane target, Color color, Color fallback, PrintStream console) {
        this.console = console;
        DefaultCaret caret = (DefaultCaret) target.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        doc = target.getStyledDocument();
        attr = new SimpleAttributeSet();
        if (color != null) {
            StyleConstants.setForeground(attr, color);
        } else {
            StyleConstants.setForeground(attr, fallback);
        }

        this.target = target;
    }

    public void setColor(Color color, Color fallback) {
        if (color != null) {
            StyleConstants.setForeground(attr, color);
        } else {
            StyleConstants.setForeground(attr, fallback);
        }
    }

    @Override
    public void flush() {
        String content = this.toString();
        try {
            if (doc.getLength() > 65536 * 2) {
                target.setText("");
            }
            doc.insertString(doc.getLength(), content, attr);
        } catch (BadLocationException e) {
//            e.printStackTrace();
            console.print(content);
            console.println(e.getMessage());
        }
        reset();
        target.setCaretPosition(doc.getLength());
    }
}
