package ru.geekbrains.client.swing;

import ru.geekbrains.client.AuthException;
import ru.geekbrains.client.Network;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginDialog extends JDialog {

    private Network network;
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JLabel lbUsername;
    private JLabel lbPassword;
    private JButton btnLogin;
    private JButton btnCancel;
    private JButton btnNewUser;

    private boolean connected;

    public LoginDialog(Frame parent, Network network) {
        super(parent, "Логин", true);
        this.network = network;

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        lbUsername = new JLabel("Имя пользователя: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        tfUsername = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        lbPassword = new JLabel("Пароль: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        pfPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pfPassword, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        btnNewUser = new JButton("Новый пользователь");
        btnLogin = new JButton("Войти");
        btnCancel = new JButton("Отмена");

        JPanel bp = new JPanel();

        bp.add(btnNewUser);
        btnNewUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    network.newUserRegistration(tfUsername.getText(), String.valueOf(pfPassword.getPassword()));
                    connected = true;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Ошибка сети",
                            "Создание нового пользователя",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (AuthException ex) {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Ошибка регистрации " + ex.getMessage(),
                            "Создание нового пользователя",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dispose();
            }
        });


        bp.add(btnLogin);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    network.authorize(tfUsername.getText(), String.valueOf(pfPassword.getPassword()));
                    connected = true;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Ошибка сети",
                            "Авторизация",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (AuthException ex) {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Ошибка авторизации" +ex.getMessage(),
                            "Авторизация",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dispose();
            }
        });

        bp.add(btnCancel);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connected = false;
                dispose();
            }
        });

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public boolean isConnected() {
        return connected;
    }
}
