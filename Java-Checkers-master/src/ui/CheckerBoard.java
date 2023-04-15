package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.Timer;

import logic.MoveGenerator;
import model.Board;
import model.Game;
import model.HumanPlayer;
import model.NetworkPlayer;
import model.Player;
import network.Command;
import network.Session;

public class CheckerBoard extends JButton {

	// Поле serialVersionUID генерируется для всех классов, реализующих интерфейс Serializable, чтобы обеспечить совместимость версий
	private static final long serialVersionUID = -6014690893709316364L;

	/** Время в миллисекундах до того, как компьютерный игрок сделает ход. */
	private static final int TIMER_DELAY = 1000;

	/** Количество пикселей отступа между границей этого компонента и доской для шашек, которая отрисовывается. */
	private static final int PADDING = 16;

	/** Игра в шашки, которая происходит на этом компоненте. */
	private Game game;

	/** Окно, содержащее этот компонент пользовательского интерфейса для игры в шашки. */
	private CheckersWindow window;

	/** Игрок, управляющий черными шашками. */
	private Player player1;

	/** Игрок, управляющий белыми шашками. */
	private Player player2;

	/** Последняя точка, которую текущий игрок выбрал на доске для шашек. */
	private Point selected;

	/** Флаг, определяющий цвет выбранной клетки. Если выбор является допустимым, используется зеленый цвет для выделения клетки. В противном случае используется красный цвет. */
	private boolean selectionValid;

	/** Цвет светлой клетки (по умолчанию, это белый цвет). */
	private Color lightTile;

	/** Цвет темной клетки (по умолчанию, это черный цвет). */
	private Color darkTile;

	/** Флаг, указывающий на окончание игры. */
	private boolean isGameOver;

	/** Таймер для управления скоростью хода компьютерного игрока. */
	private Timer timer;
	
	public CheckerBoard(CheckersWindow window) {
		this(window, new Game(), null, null);
	}
	
	public CheckerBoard(CheckersWindow window, Game game,
			Player player1, Player player2) {

		// Устанавливаем настройки компонента
		super.setBorderPainted(false);
		super.setFocusPainted(false);
		super.setContentAreaFilled(false);
		super.setBackground(Color.LIGHT_GRAY);
		this.addActionListener(new ClickListener());

		// Устанавливаем игру и цвет фоновых клеток
		this.game = (game == null)? new Game() : game;
		this.lightTile = Color.WHITE;
		this.darkTile = Color.BLACK;

		// Сохраняем ссылку на окно
		this.window = window;

		// Устанавливаем игроков, если они заданы
		setPlayer1(player1);
		setPlayer2(player2);

		// Устанавливаем обработчик событий для кликов мыши
		this.addActionListener(new ClickListener());
	}


	// Обновляет графику компонента и проверяет, закончилась ли игра.
	public void update() {
		runPlayer(); // запускает игрока, если это не игрок-человек
		this.isGameOver = game.isGameOver(); // проверяет, закончилась ли игра
		repaint(); // перерисовывает графику компонента
	}

	// Выполняет ход игрока, если это не игрок-человек
	private void runPlayer() {
		Player player = getCurrentPlayer();
		if (player == null || player.isHuman() ||
				player instanceof NetworkPlayer) { // Если игрок - человек, то ничего не делаем
			return;
		}

		// Задаем таймер для запуска игрока
		this.timer = new Timer(TIMER_DELAY, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getCurrentPlayer().updateGame(game); // обновляем игру
				timer.stop(); // останавливаем таймер
				updateNetwork(); // обновляем сеть
				update(); // обновляем компонент
			}
		});
		this.timer.start(); // запускаем таймер
	}

	// Обновление состояния игры на сервере
	public void updateNetwork() {

		// Получить соответствующие сессии для отправки
		List<Session> sessions = new ArrayList<>();
		if (player1 instanceof NetworkPlayer) {
			sessions.add(window.getSession1());
		}
		if (player2 instanceof NetworkPlayer) {
			sessions.add(window.getSession2());
		}

		// Отправляем обновление игры
		for (Session s : sessions) {
			sendGameState(s);
		}
	}
	
	public synchronized boolean setGameState(boolean testValue,
			String newState, String expected) {

		// Проверить значение, если оно запрошено
		if (testValue && !game.getGameState().equals(expected)) {
			return false;
		}

		// Обновить состояние игры
		this.game.setGameState(newState);
		repaint();
		
		return true;
	}
	
	public void sendGameState(Session s) {

		if (s == null) {
			return;
		}

		// Создаем команду и отправляем ее
		Command update = new Command(Command.COMMAND_UPDATE,
				s.getSid(), game.getGameState());
		String host = s.getDestinationHost();
		int port = s.getDestinationPort();
		update.send(host, port);
	}

	/**
	 * Рисует текущее состояние игры в шашки.
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Game game = this.game.copy();


		// Выполняем расчеты
		final int BOX_PADDING = 4;
		final int W = getWidth(), H = getHeight();
		final int DIM = W < H? W : H, BOX_SIZE = (DIM - 2 * PADDING) / 8;
		final int OFFSET_X = (W - BOX_SIZE * 8) / 2;
		final int OFFSET_Y = (H - BOX_SIZE * 8) / 2;
		final int CHECKER_SIZE = Math.max(0, BOX_SIZE - 2 * BOX_PADDING);


		// Устанавливаем цвет графического контекста на черный
		g.setColor(Color.BLACK);
		// Рисуем прямоугольник, который определяет границы шахматной доски
		g.drawRect(OFFSET_X - 1, OFFSET_Y - 1, BOX_SIZE * 8 + 1, BOX_SIZE * 8 + 1);
		// Устанавливаем цвет графического контекста на цвет ячеек светлого цвета
		g.setColor(lightTile);
		// Заполняем прямоугольник, представляющий доску, цветом ячеек светлого цвета
		g.fillRect(OFFSET_X, OFFSET_Y, BOX_SIZE * 8, BOX_SIZE * 8);
		// Устанавливаем цвет графического контекста на цвет ячеек темного цвета
		g.setColor(darkTile);
		// Проходим по каждой строке и каждому столбцу на доске
		for (int y = 0; y < 8; y ++) {
			for (int x = (y + 1) % 2; x < 8; x += 2) {
		// Заполняем прямоугольник, представляющий клетку темного цвета
				g.fillRect(OFFSET_X + x * BOX_SIZE, OFFSET_Y + y * BOX_SIZE,
						BOX_SIZE, BOX_SIZE);
			}
		}

		// Подсветка выбранной плитки, если она действительна
		if (Board.isValidPoint(selected)) {
			g.setColor(selectionValid? Color.GREEN : Color.RED);
			g.fillRect(OFFSET_X + selected.x * BOX_SIZE,
					OFFSET_Y + selected.y * BOX_SIZE,
					BOX_SIZE, BOX_SIZE);
		}

		// Рисуем шашки
		Board b = game.getBoard();
		for (int y = 0; y < 8; y ++) {
			int cy = OFFSET_Y + y * BOX_SIZE + BOX_PADDING;
			for (int x = (y + 1) % 2; x < 8; x += 2) {
				int id = b.get(x, y);

				// Пусто, просто пропустить
				if (id == Board.EMPTY) {
					continue;
				}
				
				int cx = OFFSET_X + x * BOX_SIZE + BOX_PADDING;

				// Рисование черной шашки
				if (id == Board.BLACK_CHECKER) {
					g.setColor(Color.DARK_GRAY); // Задаем цвет фона шашки
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE); // Заливаем фон шашки
					g.setColor(Color.LIGHT_GRAY); // Задаем цвет границы шашки
					g.drawOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE); // Рисуем границу шашки
					g.setColor(Color.BLACK); // Задаем цвет шашки
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE); // Заливаем шашку
					g.setColor(Color.LIGHT_GRAY); // Задаем цвет границы шашки
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE); // Рисуем границу шашки
				}
				
				// Черная дамка
				else if (id == Board.BLACK_KING) {
					g.setColor(Color.DARK_GRAY);
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.LIGHT_GRAY);
					g.drawOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.DARK_GRAY);
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.LIGHT_GRAY);
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.BLACK);
					g.fillOval(cx - 1, cy - 2, CHECKER_SIZE, CHECKER_SIZE);
				}
				
				// Белая шашка
				else if (id == Board.WHITE_CHECKER) {
					g.setColor(Color.LIGHT_GRAY);
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.DARK_GRAY);
					g.drawOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.WHITE);
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.DARK_GRAY);
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
				}
				
				// Белая дамка
				else if (id == Board.WHITE_KING) {
					g.setColor(Color.LIGHT_GRAY);
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.DARK_GRAY);
					g.drawOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.LIGHT_GRAY);
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.DARK_GRAY);
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.WHITE);
					g.fillOval(cx - 1, cy - 2, CHECKER_SIZE, CHECKER_SIZE);
				}
				
				// Any king (add some extra highlights)
				if (id == Board.BLACK_KING || id == Board.WHITE_KING) {
					g.setColor(new Color(255, 0, 0));
					g.drawOval(cx - 1, cy - 2, CHECKER_SIZE, CHECKER_SIZE);
					g.drawOval(cx + 1, cy, CHECKER_SIZE - 4, CHECKER_SIZE - 4);
				}
			}
		}

		// Рисуем знак игрока, который ходит
		String msg = game.isP1Turn()? "Ход игрока 1" : "Ход игрока 2";
		int width = g.getFontMetrics().stringWidth(msg);
		Color back = game.isP1Turn()? Color.BLACK : Color.WHITE; // Цвет фона зависит от игрока, который ходит
		Color front = game.isP1Turn()? Color.WHITE : Color.BLACK; // Цвет шрифта зависит от игрока, который ходит
		g.setColor(back);
		g.fillRect(W / 2 - width / 2 - 5, OFFSET_Y + 8 * BOX_SIZE + 2,
				width + 10, 15); // Рисуем прямоугольник под текстом
		g.setColor(front);
		g.drawString(msg, W / 2 - width / 2, OFFSET_Y + 8 * BOX_SIZE + 2 + 11); // Рисуем текст

		// Рисуем знак окончания игры
		if (isGameOver) {
			g.setFont(new Font("Arial", Font.BOLD, 20));  // Устанавливаем жирный шрифт
			msg = "Игра окончена!";
			width = g.getFontMetrics().stringWidth(msg);
			g.setColor(new Color(240, 240, 255));  // Устанавливаем цвет фона знака окончания игры
			g.fillRoundRect(W / 2 - width / 2 - 5,
					OFFSET_Y + BOX_SIZE * 4 - 16,
					width + 10, 30, 10, 10);  // Рисуем закругленный прямоугольник под текстом
			g.setColor(Color.RED);  // Устанавливаем цвет шрифта знака окончания игры
			g.drawString(msg, W / 2 - width / 2, OFFSET_Y + BOX_SIZE * 4 + 7);  // Рисуем текст
		}
	}


	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = (game == null)? new Game() : game;
	}

	public CheckersWindow getWindow() {
		return window;
	}

	public void setWindow(CheckersWindow window) {
		this.window = window;
	}

	public Player getPlayer1() {
		return player1;
	}

	public void setPlayer1(Player player1) {
		this.player1 = (player1 == null)? new HumanPlayer() : player1;
		if (game.isP1Turn() && !this.player1.isHuman()) {
			this.selected = null;
		}
	}

	public Player getPlayer2() {
		return player2;
	}

	public void setPlayer2(Player player2) {
		this.player2 = (player2 == null)? new HumanPlayer() : player2;
		if (!game.isP1Turn() && !this.player2.isHuman()) {
			this.selected = null;
		}
	}
	
	public Player getCurrentPlayer() {
		return game.isP1Turn()? player1 : player2;
	}

	// Получить цвет светлой клетки
	public Color getLightTile() {
		return lightTile;
	}

	// Установить цвет светлой клетки
	public void setLightTile(Color lightTile) {
	// Если цвет не был передан, установить белый цвет по умолчанию
		this.lightTile = (lightTile == null)? Color.WHITE : lightTile;
	}

	// Получить цвет темной клетки
	public Color getDarkTile() {
		return darkTile;
	}

	// Установить цвет темной клетки
	public void setDarkTile(Color darkTile) {
	// Если цвет не был передан, установить черный цвет по умолчанию
		this.darkTile = (darkTile == null)? Color.BLACK : darkTile;
	}

	// Обработчик клика на доске
	private void handleClick(int x, int y) {

		// Если игра окончена или текущий игрок не является человеком, завершаем выполнение функции
		if (isGameOver || !getCurrentPlayer().isHuman()) {
			return;
		}

		Game copy = game.copy();

		// Определяем, какой квадратик (если есть) был выбран
		final int W = getWidth(), H = getHeight();
		final int DIM = W < H? W : H, BOX_SIZE = (DIM - 2 * PADDING) / 8;
		final int OFFSET_X = (W - BOX_SIZE * 8) / 2;
		final int OFFSET_Y = (H - BOX_SIZE * 8) / 2;
		x = (x - OFFSET_X) / BOX_SIZE;
		y = (y - OFFSET_Y) / BOX_SIZE;
		Point sel = new Point(x, y);

		// Определяем, нужно ли попытаться выполнить ход
		if (Board.isValidPoint(sel) && Board.isValidPoint(selected)) {
			boolean change = copy.isP1Turn();
			String expected = copy.getGameState();
			boolean move = copy.move(selected, sel);
			boolean updated = (move?
					setGameState(true, copy.getGameState(), expected) : false);
			if (updated) {
				updateNetwork();
			}
			change = (copy.isP1Turn() != change);
			this.selected = change? null : sel;
		} else {
			this.selected = sel;
		}

		// Проверяем, является ли выбор корректным
		this.selectionValid = isValidSelection(
				copy.getBoard(), copy.isP1Turn(), selected);

		update();
	}


	// Этот метод проверяет, является ли выбор пользователя допустимым
	private boolean isValidSelection(Board b, boolean isP1Turn, Point selected) {

		// Базовые случаи
		int i = Board.toIndex(selected), id = b.get(i);
		if (id == Board.EMPTY || id == Board.INVALID) { // нет шашки на данной позиции
			return false;
		} else if(isP1Turn ^ (id == Board.BLACK_CHECKER ||
				id == Board.BLACK_KING)) { // неправильная шашка
			return false;
		} else if (!MoveGenerator.getSkips(b, i).isEmpty()) { // доступен прыжок
			return true;
		} else if (MoveGenerator.getMoves(b, i).isEmpty()) { // нет ходов
			return false;
		}

		// Определить, есть ли доступный прыжок для другой шашки
		List<Point> points = b.find(
				isP1Turn? Board.BLACK_CHECKER : Board.WHITE_CHECKER);
		points.addAll(b.find(
				isP1Turn? Board.BLACK_KING : Board.WHITE_KING));
		for (Point p : points) {
			int checker = Board.toIndex(p);
			if (checker == i) {
				continue;
			}
			if (!MoveGenerator.getSkips(b, checker).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	// Создаем внутренний класс, реализующий интерфейс ActionListener для обработки событий клика мыши
	private class ClickListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			// Получаем новые координаты мыши и обрабатываем клик
			Point m = CheckerBoard.this.getMousePosition();
			if (m != null) {
				handleClick(m.x, m.y);
			}
		}
	}
}
