package network;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import model.NetworkPlayer;
import ui.CheckerBoard;
import ui.CheckersWindow;
import ui.NetworkWindow;
import ui.OptionPanel;

public class CheckersNetworkHandler implements ActionListener {

	private static final int MIN_SID_LENGTH = 16; // минимальная длина идентификатора сессии

	private static final int MAX_SID_LENGTH = 64; // максимальная длина идентификатора сессии

	public static final String RESPONSE_ACCEPTED = "ACCEPTED"; // ответ, если запрос принят

	public static final String RESPONSE_DENIED = "DENIED"; // ответ, если запрос отклонён

	private boolean isPlayer1; // true, если это игрок 1, иначе false

	private CheckersWindow window; // ссылка на главное окно Checkers

	private CheckerBoard board; // ссылка на игровую доску

	private OptionPanel opts; // ссылка на панель опций

	public CheckersNetworkHandler(boolean isPlayer1, CheckersWindow window,
								  CheckerBoard board, OptionPanel opts) {
		this.isPlayer1 = isPlayer1;
		this.window = window;
		this.board = board;
		this.opts = opts;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// Invalid event
		if (e == null || !(e.getSource() instanceof ConnectionHandler)) { // проверяем, что источником является ConnectionHandler
			return;
		}

		// Get the data from the connection
		ConnectionHandler handler = (ConnectionHandler) e.getSource(); // получаем ConnectionHandler
		String data = ConnectionListener.read(handler.getSocket()); // считываем данные из потока
		data = data.replace("\r\n", "\n"); // заменяем перенос строки в формате "\r\n" на "\n"

		// Unable to handle
		if (window == null || board == null || opts == null) { // проверяем, что ссылки на объекты не null
			sendResponse(handler, "Ошибка клиента: неверный сетевой обработчик."); // отправляем сообщение об ошибке клиенту
			return;
		}

		Session s1 = window.getSession1(), s2 = window.getSession2(); // получаем ссылки на объекты Session

		// Determine if a valid user
		String[] lines = data.split("\n"); // разбиваем строку на массив строк по символу переноса строки
		String cmd = lines[0].split(" ")[0].toUpperCase(); // получаем команду (первое слово из первой строки) и переводим в верхний регистр
		String sid = lines.length > 1? lines[1] : ""; // получаем идентификатор сессии, если он есть
		String response = ""; // создаём строку для ответа клиенту
		boolean match = false; // флаг для проверки совпадения идентификатор

		if (isPlayer1) {
			match = sid.equals(s1.getSid()); // Проверяем, является ли первый игрок отправителем сообщения
		} else {
			match = sid.equals(s2.getSid()); // Проверяем, является ли второй игрок отправителем сообщения
		}

// Клиент хочет обновить игровую доску
		if (cmd.equals(Command.COMMAND_UPDATE)) {
			String newState = (match && lines.length > 2? lines[2] : ""); // Состояние игровой доски для обновления, если это запрос от того же игрока, который подключен к доске
			response = handleUpdate(newState); // Обрабатываем обновление доски
		}

// Клиент хочет подключиться к игре
		else if (cmd.equals(Command.COMMAND_CONNECT)) {

			// Получаем порт, который был передан (в поле SID)
			int port = -1;
			try {
				port = Integer.parseInt(sid); // парсим номер порта из sid
			} catch (NumberFormatException err) {}

			// Определяем, является ли клиент, пытающийся подключиться, первым игроком
			String isP1 = (lines.length > 2? lines[2] : "");
			boolean remotePlayer1 = isP1.startsWith("1"); // проверяем, является ли подключающийся игрок первым игроком

			// Обрабатываем запрос на подключение
			response = handleConnect(handler.getSocket(), port, remotePlayer1); // обрабатываем запрос на подключение
		}

// Клиент запрашивает текущее состояние игры
		else if (cmd.equals(Command.COMMAND_GET)) {

			// Отправляем игровую доску, если SID соответствует
			if (match) {
				response = RESPONSE_ACCEPTED + "\n"
						+ board.getGame().getGameState(); // отправляем состояние игровой доски
			} else {
				response = RESPONSE_DENIED; // Отправляем отказ
			}
		}

// Клиент хочет отключиться
		else if (cmd.equals(Command.COMMAND_DISCONNECT)) {

			// Отключаем, если SID соответствует
			if (match) {
				response = RESPONSE_ACCEPTED + "\nКлиент был отключен."; // отправляем подтверждение отключения
				if (isPlayer1) {
					s1.setSid(null); // Удаляем sid первого игрока
					this.opts.getNetworkWindow1().setCanUpdateConnect(true);
				} else {
					s2.setSid(null); // Удаляем sid второго игрока
					this.opts.getNetworkWindow2().setCanUpdateConnect(true);
				}
			} else {
				response = RESPONSE_DENIED + "\nОшибка: отключение невозможно, если нет подключения.";
			}
		}

// Недопустимая команда
		else {
			response = RESPONSE_DENIED + "\nJava Checkers - неизвестная команда '" + cmd + "'"; // Неизвестная команда
		}

		// Send the response to whoever connected
		sendResponse(handler, response);
	}

	private String handleUpdate(String newState) {

		// Новое состояние недействительно
		if (newState.isEmpty()) {
			return RESPONSE_DENIED;
		}

		// Обновляем игровое состояние текущего клиента
		this.board.setGameState(false, newState, null);
		if (!board.getCurrentPlayer().isHuman()) {
			board.update();
		}

		// Проверяем, являются ли оба игрока сетевыми игроками
		// Если да, пересылаем игровое состояние (т.е. этот клиент выступает в роли роутера)
		if (isPlayer1 &&
				board.getPlayer2() instanceof NetworkPlayer) {
			board.sendGameState(window.getSession2());
		} else if (!isPlayer1 &&
				board.getPlayer1() instanceof NetworkPlayer) {
			board.sendGameState(window.getSession1());
		}

		return RESPONSE_ACCEPTED;
	}

	private String handleConnect(Socket s, int port, boolean remotePlayer1) {

		// Проверяем, подключился ли уже кто-то другой
		Session s1 = window.getSession1(), s2 = window.getSession2();
		String sid1 = s1.getSid();
		String sid2 = s2.getSid();
		if ((isPlayer1 && sid1 != null && !sid1.isEmpty()) ||
				(!isPlayer1 && sid2 != null && !sid2.isEmpty())) {
			return RESPONSE_DENIED + "\nОшибка: пользователь уже подключен.";
		}

		// Проверяем, является ли подключение допустимым
		if (!(isPlayer1 ^ remotePlayer1)) {
			return RESPONSE_DENIED + "\nОшибка: другой клиент уже игрок " + (remotePlayer1? "1." : "2.");
		}
		String host = s.getInetAddress().getHostAddress();
		if (host.equals("127.0.0.1")) {
			if ((isPlayer1 && port == s2.getSourcePort()) ||
					(!isPlayer1 && port == s1.getSourcePort())) {
				return RESPONSE_DENIED + "\nОшибка: клиент не может подключиться к самому себе.";
			}
		}

		// Обновляем соединение
		String sid = generateSessionID();
		Session session = isPlayer1? s1 : s2;
		NetworkWindow win = (isPlayer1?
				opts.getNetworkWindow1() : opts.getNetworkWindow2());
		session.setSid(sid);
		session.setDestinationHost(host);
		session.setDestinationPort(port);

		// Обновляем пользовательский интерфейс
		win.setDestinationHost(host);
		win.setDestinationPort(port);
		win.setCanUpdateConnect(false);
		win.setMessage("  Подключено к " + host + ":" + port + ".");

		return RESPONSE_ACCEPTED + "\n" + sid + "\nУспешно подключено.";

	}

	private static void sendResponse(ConnectionHandler handler,
									 String response) {

		// Простые случаи
		if (handler == null) {
			return;
		}
		Socket s = handler.getSocket();
		if (s == null || s.isClosed()) {
			return;
		}
		if (response == null) {
			response = "";
		}

		// Написать ответ и закрыть соединение
		try (OutputStream os = s.getOutputStream()) {
			os.write(response.getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			// Закрыть сокет
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String generateSessionID() {

		// Сгенерировать строку случайной длины
		String sid = "";
		int chars = (int) ((MAX_SID_LENGTH - MIN_SID_LENGTH) * Math.random())
				+ MIN_SID_LENGTH;
		for (int i = 0; i < chars; i ++) {

			// Сгенерировать символ в случайном диапазоне
			int t = (int) (4 * Math.random());
			int min = 32, max = 48;
			if (t == 1) {
				min = 48;
				max = 65;
			} else if (t == 2) {
				min = 65;
				max = 97;
			} else if (t == 3) {
				min = 97;
				max = 125;
			}
			char randChar = (char) ((Math.random() * (max - min)) + min);
			sid += randChar;
		}

		return sid;
	}
}
