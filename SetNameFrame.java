package NIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @Time : 2018/4/8 21:31
 * @Author : Dallo Gao
 * @File : SetNameFrame.java
 * @Software: IntelliJ IDEA
 **/
public class SetNameFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static JTextField txtName;// 文本框

    private SetNameFrame() {
        this.setLayout(null);
        Toolkit kit = Toolkit.getDefaultToolkit();
        int w = kit.getScreenSize().width;
        int h = kit.getScreenSize().height;
        this.setBounds(w / 2 - 230 / 2, h / 2 - 200 / 2, 230, 200);
        this.setTitle("设置名称");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(false);
        txtName = new JTextField(4);
        this.add(txtName);
        txtName.setBounds(10, 10, 100, 25);
        JButton btnOK = new JButton("OK");
        this.add(btnOK);
        btnOK.setBounds(120, 10, 80, 25);
        JLabel label = new JLabel("[w:" + w + ",h:" + h + "]");
        this.add(label);
        label.setBounds(10, 40, 200, 100);
        label.setText("<html>在上面的文本框中输入名字<br/>显示器宽度：" + w + "<br/>显示器高度：" + h
                + "</html>");

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String uname = txtName.getText();
                ClientService service = ClientService.getInstance();
                ChatFrame chatFrame = new ChatFrame(service, uname);
                chatFrame.show();
                setVisible(false);
            }
        });
    }

    public static void main(String[] args) {
        SetNameFrame setNameFrame = new SetNameFrame();
        setNameFrame.setVisible(true);
    }

}