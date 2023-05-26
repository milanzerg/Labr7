package bsu.rfe.java.group6.lab6.Kuksa.varA3;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500, FRAME_MINIMUM_HEIGHT = 500;
    private static final int FROM_FIELD_DEFAULT_COLUMNS = 10, TO_FIELD_DEFAULT_COLUMNS = 20;
    private static final int INCOMING_AREA_DEFAULT_ROWS = 10, OUTGOING_AREA_DEFAULT_ROWS = 5;
    private static final int SMALL_GAP = 5, MEDIUM_GAP = 10, LARGE_GAP = 15;
    static int SERVER_PORT = 4567;
    private final JTextField textFieldFrom, textFieldTo;
    private final JTextArea textAreaIncoming, textAreaOutgoing;

    public MainFrame() {
        super(FRAME_TITLE);
        setMinimumSize(new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT)); // Центрирование окна
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2, (kit.getScreenSize().height - getHeight()) / 2);
        textAreaIncoming = new JTextArea(INCOMING_AREA_DEFAULT_ROWS, 0); // Текстовая область для отображения полученных сообщений
        textAreaIncoming.setEditable(false);
        final JScrollPane scrollPaneIncoming = new JScrollPane(textAreaIncoming); // Контейнер, обеспечивающий прокрутку текстовой области
        final JLabel labelFrom = new JLabel("Подпись"), labelTo = new JLabel("Получатель"); // Подписи полей
        textFieldFrom = new JTextField(FROM_FIELD_DEFAULT_COLUMNS); // Поля ввода имени пользователя
        textFieldTo = new JTextField(TO_FIELD_DEFAULT_COLUMNS); // Поля ввода адреса получателя
        textAreaOutgoing = new JTextArea(OUTGOING_AREA_DEFAULT_ROWS, 0); // Текстовая область для ввода сообщения
        final JScrollPane scrollPaneOutgoing = new JScrollPane(textAreaOutgoing); // Контейнер, обеспечивающий прокрутку текстовой области
        final JPanel messagePanel = new JPanel(); // Панель ввода сообщения
        messagePanel.setBorder(BorderFactory.createTitledBorder("Сообщение"));
        final JButton sendButton = new JButton("Отправить"); // Кнопка отправки сообщения
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        final GroupLayout layout2 = new GroupLayout(messagePanel); // Компоновка элементов панели "Сообщение"
        messagePanel.setLayout(layout2);
        layout2.setHorizontalGroup(layout2.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout2.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(layout2.createSequentialGroup()
                                .addComponent(labelFrom)
                                .addGap(SMALL_GAP)
                                .addComponent(textFieldFrom)
                                .addGap(LARGE_GAP)
                                .addComponent(labelTo)
                                .addGap(SMALL_GAP)
                                .addComponent(textFieldTo))
                        .addComponent(scrollPaneOutgoing)
                        .addComponent(sendButton))
                .addContainerGap());
        layout2.setVerticalGroup(layout2.createSequentialGroup()
                .addContainerGap().addGroup(layout2.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelFrom)
                        .addComponent(textFieldFrom)
                        .addComponent(labelTo)
                        .addComponent(textFieldTo))
                .addGap(MEDIUM_GAP)
                .addComponent(scrollPaneOutgoing)
                .addGap(MEDIUM_GAP)
                .addComponent(sendButton)
                .addContainerGap());
        final GroupLayout layout1 = new GroupLayout(getContentPane()); // Компоновка элементов фрейма
        setLayout(layout1);
        layout1.setHorizontalGroup(layout1.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout1.createParallelGroup()
                        .addComponent(scrollPaneIncoming)
                        .addComponent(messagePanel))
                .addContainerGap());
        layout1.setVerticalGroup(layout1.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneIncoming)
                .addGap(MEDIUM_GAP)
                .addComponent(messagePanel)
                .addContainerGap());
        new Thread(new Runnable() { // Создание и запуск потока-обработчика запросов
            @Override
            public void run() {
                try {
                    final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
                    while (!Thread.interrupted()) {
                        final Socket socket = serverSocket.accept();
                        final DataInputStream in = new DataInputStream(socket.getInputStream());
                        final String senderName = in.readUTF(); // Читаем имя отправителя
                        final String message = in.readUTF(); // Читаем сообщение
                        socket.close(); // Закрываем соединение
                        final String address = ((InetSocketAddress) socket // Выделяем IP-адрес
                                        .getRemoteSocketAddress())
                                        .getAddress()
                                        .getHostAddress();
                        textAreaIncoming.append(senderName + " (" + address + "): " + message + "\n"); // Выводим сообщение в текстовую область
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this, "Ошибка в работе сервера", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();
    }

    private void sendMessage() {
        try {
            String senderName = textFieldFrom.getText(); // Получаем необходимые параметры
            final String destinationAddressPlus = textFieldTo.getText(); // -||-
            int length = destinationAddressPlus.length();
            int index = destinationAddressPlus.indexOf(':');
            final String destinationAddress = destinationAddressPlus.substring(0, index);
            SERVER_PORT = Integer.parseInt(destinationAddressPlus.substring(index + 1, length));
            final String message = textAreaOutgoing.getText(); // -||-

            if (senderName.isEmpty()) { // Убеждаемся, что поля не пустые
                JOptionPane.showMessageDialog(this, "Введите имя отправителя", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (destinationAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите адрес узла-получателя", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите текст сообщения", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final Socket socket = new Socket(destinationAddress, SERVER_PORT); // Создаем сокет для соединения
            final DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // Открываем поток вывода данных
            out.writeUTF(senderName); // Записываем в поток имя
            out.writeUTF(message); // Записываем в поток сообщение
            socket.close(); // Закрываем сокет
            textAreaIncoming.append("Я -> " + destinationAddress + ": " + message + "\n"); // Помещаем сообщения в текстовую область вывода
            textAreaOutgoing.setText(""); // Очищаем текстовую область ввода сообщения
        } catch (UnknownHostException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this, "Не удалось отправить сообщение: узел-адресат не найден", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this, "Не удалось отправить сообщение", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final MainFrame frame = new MainFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }
}