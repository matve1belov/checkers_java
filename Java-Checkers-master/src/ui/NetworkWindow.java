package ui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NetworkWindow extends JFrame {

	//Окно настроек сети
	private static final long serialVersionUID = -3680869784531557351L;

	// ширина окна по умолчанию
	public static final int DEFAULT_WIDTH = 400;

	// высота окна по умолчанию
	public static final int DEFAULT_HEIGHT = 140;

	// название окна по умолчанию
	public static final String DEFAULT_TITLE = "Configure Network";

	// ID, отправленный слушателю действий при нажатии на кнопку "Connect"
	public static final int CONNECT_BUTTON = 0;

	// ID, отправленный слушателю действий при нажатии на кнопку "Listen"
	public static final int LISTEN_BUTTON = 1;

	// текстовое поле для исходного порта
	private JTextField srcPort;
	
	/** текстовое поле для названия или IP-адреса удаленного хоста */
	private JTextField destHost;
	
	/** текстовое поле для удаленного порта */
	private JTextField destPort;
	
	/** кнопка, которая используется для указания того, что клиент должен начать прослушивать порт, указанный в srcPort */
	private JButton listen;
	
	/** кнопка, которая используется для указания того, что клиент должен попытаться подключиться к удаленному хосту/порту, указанному в destHost и destPort */
	private JButton connect;

	/** Панель, содержащая все компоненты для настроек этого клиента. */
	private JPanel src;

	/** Панель, содержащая все компоненты для удаленного клиента
	 * настройки. */
	private JPanel dest;

	/** Метка для отображения сообщения в окне. */
	private JLabel msg;

	/** Слушатель действий, который вызывается, когда «Слушать» или «Подключиться»
	 * щелкнул. */
	private ActionListener actionListener;

	// Конструктор окна по умолчанию
	public NetworkWindow() {
// Вызываем конструктор родительского класса и задаем параметры окна
		super(DEFAULT_TITLE);
		super.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		super.setLocationByPlatform(true);
// Вызываем метод для инициализации компонентов окна
		init();
	}

	// Конструктор окна с переданным в него actionListener'ом
	public NetworkWindow(ActionListener actionListener) {
	// Вызываем конструктор окна по умолчанию
		this();
	// Устанавливаем переданный actionListener
		this.actionListener = actionListener;
	}

	// Конструктор окна с переданными параметрами
	public NetworkWindow(ActionListener actionListener, int srcPort, String destHost, int destPort) {
	// Вызываем конструктор окна по умолчанию
		this();
	// Устанавливаем переданный actionListener
		this.actionListener = actionListener;
	// Устанавливаем переданные параметры
		setSourcePort(srcPort);
		setDestinationHost(destHost);
		setDestinationPort(destPort);
	}

	/** Инициализация компонентов, которые будут отображаться в окне. */
	private void init() {

		// Установка компонентов
		this.getContentPane().setLayout(new GridLayout(3, 1));
		this.srcPort = new JTextField(4); // текстовое поле для исходного порта
		this.destHost = new JTextField(11); // текстовое поле для хоста назначения
		this.destHost.setText("127.0.0.1"); // установка значения поля хоста назначения
		this.destPort = new JTextField(4); // текстовое поле для порта назначения
		this.listen = new JButton("Listen"); // кнопка для прослушивания порта
		this.listen.addActionListener(new ButtonListener()); // добавление слушателя для кнопки Listen
		this.connect = new JButton("Connect"); // кнопка для соединения с хостом назначения
		this.connect.addActionListener(new ButtonListener()); // добавление слушателя для кнопки Connect
		this.src = new JPanel(new FlowLayout(FlowLayout.LEFT)); // панель для компонентов, связанных с исходным портом
		this.dest = new JPanel(new FlowLayout(FlowLayout.LEFT)); // панель для компонентов, связанных с хостом и портом назначения
		this.msg = new JLabel(); // метка для отображения сообщений
		this.src.add(new JLabel("Source port:")); // добавление метки для исходного порта на панель исходного порта
		this.src.add(srcPort); // добавление текстового поля для исходного порта на панель исходного порта
		this.src.add(listen); // добавление кнопки Listen на панель исходного порта
		this.dest.add(new JLabel("Destination host/port:")); // добавление метки для хоста и порта назначения на панель хоста и порта назначения
		this.dest.add(destHost); // добавление текстового поля для хоста назначения на панель хоста и порта назначения
		this.dest.add(destPort); // добавление текстового поля для порта назначения на панель хоста и порта назначения
		this.dest.add(connect); // добавление кнопки Connect на панель хоста и порта назначения
		setCanUpdateConnect(false); // установка возможности обновления соединения в false

		// Добавление подсказок
		this.srcPort.setToolTipText("Исходный порт для прослушивания обновлений (1025 - 65535)");
		this.destPort.setToolTipText("Порт назначения для прослушивания обновлений (1025 - 65535)");
		this.destHost.setToolTipText("Хост назначения для отправки обновлений (например, localhost)");

		createLayout(null);
	}


	private void createLayout(String msg) {
		
		this.getContentPane().removeAll();

		// Добавляем соответствующие компоненты
		this.getContentPane().add(src);
		this.getContentPane().add(dest);
		this.msg.setText(msg);
		this.getContentPane().add(this.msg);
		this.msg.setVisible(false);
		this.msg.setVisible(true);
	}

	public void setCanUpdateListen(boolean canUpdate) {
		this.srcPort.setEnabled(canUpdate);
		this.listen.setEnabled(canUpdate);
	}

	public void setCanUpdateConnect(boolean canUpdate) {
		this.destHost.setEnabled(canUpdate);
		this.destPort.setEnabled(canUpdate);
		this.connect.setEnabled(canUpdate);
	}

	public ActionListener getActionListener() {
		return actionListener;
	}

	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

	public int getSourcePort() {
		return parseField(srcPort);
	}

	public void setSourcePort(int port) {
		this.srcPort.setText("" + port);
	}

	public String getDestinationHost() {
		return destHost.getText();
	}

	public void setDestinationHost(String host) {
		this.destHost.setText(host);
	}

	public int getDestinationPort() {
		return parseField(destPort);
	}

	public void setDestinationPort(int port) {
		this.destPort.setText("" + port);
	}

	public String getMessage() {
		return msg.getText();
	}

	public void setMessage(String message) {
		createLayout(message);
	}

	// Метод для парсинга значений из текстового поля в целое число
	private static int parseField(JTextField tf) {

		if (tf == null) { // Если текстовое поле не было передано в метод, вернуть 0
			return 0;
		}

		// Попытаться преобразовать текстовое значение в целое число
		int val = 0;
		try {
			val = Integer.parseInt(tf.getText()); // Преобразование строки в число
		} catch (NumberFormatException e) {} // Если не удалось преобразовать, ничего не делать и вернуть 0

		return val; // Вернуть результат преобразования
	}

	// Класс-слушатель для кнопки
	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (actionListener != null) { // Если слушатель действий установлен, выполняем следующий код
				JButton src = (JButton) e.getSource(); // Получаем источник события - кнопку
				ActionEvent event = null;
				if (src == listen) { // Если источником является кнопка "слушать"
					event = new ActionEvent(NetworkWindow.this,
							LISTEN_BUTTON, null); // Создаем новое событие с идентификатором "слушать"
				} else { // Если источником является кнопка "соединить"
					event = new ActionEvent(NetworkWindow.this,
							CONNECT_BUTTON, null); // Создаем новое событие с идентификатором "соединить"
				}
				actionListener.actionPerformed(event); // Вызываем метод для обработки события
			}
		}
	}
}
