package ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import model.Player;
import network.CheckersNetworkHandler;
import network.ConnectionListener;
import network.Session;

public class CheckersWindow extends JFrame {

	// версия класса
	private static final long serialVersionUID = 8782122389400590079L;

	/**
	 * Ширина окна по умолчанию.
	 */
	public static final int DEFAULT_WIDTH = 500;

	/**
	 * Высота окна по умолчанию.
	 */
	public static final int DEFAULT_HEIGHT = 600;

	/**
	 * Заголовок окна по умолчанию.
	 */
	public static final String DEFAULT_TITLE = "Шашки";

	/**
	 * Поле шашек.
	 */
	private CheckerBoard board;

	// Панель настроек
	private OptionPanel opts;

	// Сессии для подключения к серверу
	private Session session1;
	private Session session2;

	// Конструкторы
	public CheckersWindow() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_TITLE);
	}

	public CheckersWindow(Player player1, Player player2) {
		this();
		setPlayer1(player1);
		setPlayer2(player2);
	}

	public CheckersWindow(int width, int height, String title) {

		// Устанавливаем параметры окна
		super(title);
		super.setSize(width, height);
		super.setLocationByPlatform(true);

		// Создаем компоненты
		JPanel layout = new JPanel(new BorderLayout());
		this.board = new CheckerBoard(this);
		this.opts = new OptionPanel(this);
		layout.add(board, BorderLayout.CENTER);
		layout.add(opts, BorderLayout.SOUTH);
		this.add(layout);

		// Настраиваем обработчики для сетевого взаимодействия
		CheckersNetworkHandler session1Handler, session2Handler;
		session1Handler = new CheckersNetworkHandler(true, this, board, opts);
		session2Handler = new CheckersNetworkHandler(false, this, board, opts);
		this.session1 = new Session(new ConnectionListener(
				0, session1Handler), null, null, -1);
		this.session2 = new Session(new ConnectionListener(
				0, session2Handler), null, null, -1);
	}

	// Геттеры и сеттеры
	public CheckerBoard getBoard() {
		return board;
	}

	public void setPlayer1(Player player1) {
		this.board.setPlayer1(player1);
		this.board.update();
	}

	public void setPlayer2(Player player2) {
		this.board.setPlayer2(player2);
		this.board.update();
	}

	public void restart() {
		this.board.getGame().restart();
		this.board.update();
	}

	public void setGameState(String state) {
		this.board.getGame().setGameState(state);
	}

	public Session getSession1() {
		return session1;
	}

	public Session getSession2() {
		return session2;
	}
}
