package ui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


import model.HumanPlayer;
import model.NetworkPlayer;
import model.Player;
import network.CheckersNetworkHandler;
import network.Command;
import network.ConnectionListener;
import network.Session;

// Класс, представляющий панель опций
public class OptionPanel extends JPanel {

	//Уникальный идентификатор сериализации для сохранения состояния объекта
	private static final long serialVersionUID = -4763875452164030755L;

	/**  Окно с игрой в шашки для обновления при изменении опций */
	private CheckersWindow window;
	
	/** Кнопка, которая при нажатии перезапускает игру */
	private JButton restartBtn;
	
	/** Комбо-бокс, который меняет тип игрока 1 */
	private JComboBox<String> player1Opts;
	
	/** Окно сетевых опций для игрока 1 */
	private NetworkWindow player1Net;
	
	/** Кнопка для выполнения действия в зависимости от типа игрока */
	private JButton player1Btn;

	/** Комбо-бокс, который меняет тип игрока 2 */
	private JComboBox<String> player2Opts;

	/** Окно сетевых опций для игрока 2 */
	private NetworkWindow player2Net;
	
	/** Кнопка для выполнения действия в зависимости от типа игрока */
	private JButton player2Btn;

	/**
	 * Создает новую панель опций для указанного окна игры в шашки.
	 *
	 * @param window окно с игрой в шашки для обновления
	 */
	public OptionPanel(CheckersWindow window) {
		super(new GridLayout(0, 1));
		// Конструктор класса, принимающий в качестве аргумента объект окна игры в шашки
		this.window = window;

		// Здесь будет происходить инициализация всех элементов панели опций и присваивание им значений
		OptionListener ol = new OptionListener();
		final String[] playerTypeOpts = {"Пользователь", "По сети"};
		this.restartBtn = new JButton("Рестарт");
		this.player1Opts = new JComboBox<>(playerTypeOpts);
		this.player2Opts = new JComboBox<>(playerTypeOpts);
		this.restartBtn.addActionListener(ol);
		this.player1Opts.addActionListener(ol);
		this.player2Opts.addActionListener(ol);
		JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel middle = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		this.player1Net = new NetworkWindow(ol);
		this.player1Net.setTitle("Player 1 - Configure Network");
		this.player2Net = new NetworkWindow(ol);
		this.player2Net.setTitle("Player 2 - Configure Network");
		this.player1Btn = new JButton("Set Connection");
		this.player1Btn.addActionListener(ol);
		this.player1Btn.setVisible(false);
		this.player2Btn = new JButton("Set Connection");
		this.player2Btn.addActionListener(ol);
		this.player2Btn.setVisible(false);
		
		// Добавляем компоненты в макет
		top.add(restartBtn);
		middle.add(new JLabel("(black) Player 1: "));
		middle.add(player1Opts);
		middle.add(player1Btn);
		bottom.add(new JLabel("(white) Player 2: "));
		bottom.add(player2Opts);
		bottom.add(player2Btn);
		this.add(top);
		this.add(middle);
		this.add(bottom);
	}

	public CheckersWindow getWindow() {
		return window;
	}

	public void setWindow(CheckersWindow window) {
		this.window = window;
	}
	
	public void setNetworkWindowMessage(boolean forPlayer1, String msg) {
		if (forPlayer1) {
			this.player1Net.setMessage(msg);
		} else {
			this.player2Net.setMessage(msg);
		}
	}
	
	public NetworkWindow getNetworkWindow1() {
		return player1Net;
	}
	
	public NetworkWindow getNetworkWindow2() {
		return player2Net;
	}

	/**
	 Обрабатывает события сети.
	 @param win Окно сети, из которого было получено событие
	 @param e Событие
	 */
	private void handleNetworkUpdate(NetworkWindow win, ActionEvent e) {
		// Проверяем, что полученные параметры не null
		if (win == null || window == null || e == null) {
			return;
		}

		// Получаем информацию о портах и адресе хоста, а также
		// определяем игрока, который вызвал событие
		int srcPort = win.getSourcePort(), destPort = win.getDestinationPort();
		String destHost = win.getDestinationHost();
		boolean isPlayer1 = (win == player1Net);
		Session s = (isPlayer1? window.getSession1() : window.getSession2());
		
		// Настройка нового порта для прослушивания
		if (e.getID() == NetworkWindow.LISTEN_BUTTON) {

			// Проверяем диапазон портов и доступность порта
			if (srcPort < 1025 || srcPort > 65535) {
				win.setMessage("  Error: source port must be"
						+ " between 1025 and 65535. ");
				return;
			}
			if (!ConnectionListener.available(srcPort)) {
				win.setMessage("  Error: source port " + srcPort+ " is not available.");
				return;
			}

			// Обновляем сервер, если необходимо
			if (s.getListener().getPort() != srcPort) {
				s.getListener().stopListening();
			}
			s.getListener().setPort(srcPort);
			s.getListener().listen();
			win.setMessage("  This client is listening on port " + srcPort);
			win.setCanUpdateListen(false);
			win.setCanUpdateConnect(true);
		}
		
		// Пробуем подключиться
		else if (e.getID() == NetworkWindow.CONNECT_BUTTON) {
			
			// Проверяем порт и хост
			if (destPort < 1025 || destPort > 65535) {
				win.setMessage("  Error: destination port must be "
						+ "between 1025 and 65535. ");
				return;
			}
			if (destHost == null || destHost.isEmpty()) {
				destHost = "127.0.0.1";
			}

			//Подключиться к предложенному хосту
			Command connect = new Command(Command.COMMAND_CONNECT,
					win.getSourcePort() + "", isPlayer1? "1" : "0");
			String response = connect.send(destHost, destPort);
			
			// Нет ответа
			if (response.isEmpty()) {
				win.setMessage("  Error: could not connect to " + destHost +
						":" + destPort + ".");
			}
			
			// Это был действующий клиент, но он отказался подключаться
			else if (response.startsWith(CheckersNetworkHandler.RESPONSE_DENIED)) {
				String[] lines = response.split("\n");
				String errMsg = lines.length > 1? lines[1] : "";
				if (errMsg.isEmpty()) {
					win.setMessage("  Ошибка: другой клиент отказался подключаться.");
				} else {
					win.setMessage("  " + errMsg);
				}
			}

			//Соединение было принято клиентом
			else if (response.startsWith(CheckersNetworkHandler.RESPONSE_ACCEPTED)){
				
				// Update the session
				s.setDestinationHost(destHost);
				s.setDestinationPort(destPort);
				win.setMessage("  Успешно начал сеанс с " +
						destHost + ":" + destPort + ".");
				win.setCanUpdateConnect(false);

				//Обновление SID
				String[] lines = response.split("\n");
				String sid = lines.length > 1? lines[1] : "";
				s.setSid(sid);
				
				// Получить новое состояние игры
				Command get = new Command(Command.COMMAND_GET, sid, null);
				response = get.send(destHost, destPort);
				lines = response.split("\n");
				String state = lines.length > 1? lines[1] : "";
				window.setGameState(state);
			}

			// Общая ошибка, возможно, пользователь попробовал веб-сервер и
			// ответ является HTTP-ответом
			else {
				win.setMessage("  Ошибка: вы пытались подключиться к хосту и \"\n" +
						"+ \"порт, на котором не был запущен клиент шашек.");
			}
		}
	}

	private static Player getPlayer(JComboBox<String> playerOpts) {
		
		Player player = new HumanPlayer();
		if (playerOpts == null) {
			return player;
		}
		
		// Определяем тип
		String type = "" + playerOpts.getSelectedItem();
		if (type.equals("По сети")) {
			player = new NetworkPlayer();
		}
		
		return player;
	}
	

	private class OptionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			// Нет окна для обновления
			if (window == null) {
				return;
			}
			
			Object src = e.getSource();

			//Обработка действий пользователя
			JButton btn = null;
			boolean isNetwork = false, isP1 = true;
			Session s = null;
			if (src == restartBtn) {
				window.restart();
				window.getBoard().updateNetwork();
			} else if (src == player1Opts) {
				Player player = getPlayer(player1Opts);
				window.setPlayer1(player);
				isNetwork = (player instanceof NetworkPlayer);
				btn = player1Btn;
				s = window.getSession1();
			} else if (src == player2Opts) {
				Player player = getPlayer(player2Opts);
				window.setPlayer2(player);
				isNetwork = (player instanceof NetworkPlayer);
				btn = player2Btn;
				s = window.getSession2();
				isP1 = false;
			} else if (src == player1Btn) {
				player1Net.setVisible(true);
			} else if (src == player2Btn) {
				player2Net.setVisible(true);
			}
			
			// Обработка обновления сети
			else if (src == player1Net || src == player2Net) {
				handleNetworkUpdate((NetworkWindow) src, e);
			}
			
			// Обновить пользовательский интерфейс
			if (btn != null) {

				//Отключить при необходимости
				String sid = s.getSid();
				if (!isNetwork && btn.isVisible() &&
						sid != null && !sid.isEmpty()) {

					//Отправить запрос
					Command disconnect = new Command(
							Command.COMMAND_DISCONNECT, sid);
					disconnect.send(
							s.getDestinationHost(), s.getDestinationPort());
					
					// Обновить сессию
					s.setSid(null);
					NetworkWindow win = isP1? player1Net : player2Net;
					win.setCanUpdateConnect(true);
				}
				
				// Обновить графический интерфейс
				btn.setVisible(isNetwork);
				btn.repaint();
			}
		}
	}
}
