package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Board {

	public static final int INVALID = -1; // Константа, обозначающая некорректное значение
	public static final int EMPTY = 0; // Константа, обозначающая пустое поле
	public static final int BLACK_CHECKER = 4 * 1 + 2 * 1 + 1 * 0; // Константа, обозначающая черную шашку
	public static final int WHITE_CHECKER = 4 * 1 + 2 * 0 + 1 * 0; // Константа, обозначающая белую шашку
	public static final int BLACK_KING = 4 * 1 + 2 * 1 + 1 * 1; // Константа, обозначающая черного короля
	public static final int WHITE_KING = 4 * 1 + 2 * 0 + 1 * 1; // Константа, обозначающая белого короля

	private int[] state; // Массив состояний доски (три элемента)

	// Конструктор класса
	public Board() {
		reset();
	}

	// Создает копию объекта Board
	public Board copy() {
		Board copy = new Board();
		copy.state = state.clone();
		return copy;
	}

	// Метод, сбрасывающий доску в начальное состояние
	public void reset() {

		// Сброс состояния доски
		this.state = new int[3];
		for (int i = 0; i < 12; i ++) {
			set(i, BLACK_CHECKER); // На черные клетки в начале игры ставятся черные шашки
			set(31 - i, WHITE_CHECKER); // На белые клетки в начале игры ставятся белые шашки
		}
	}

	// Метод, находящий все фигуры на доске с определенным ID
	public List<Point> find(int id) {

		// Находим все черные клетки с соответствующим ID
		List<Point> points = new ArrayList<>();
		for (int i = 0; i < 32; i ++) {
			if (get(i) == id) {
				points.add(toPoint(i));
			}
		}

		return points;
	}

	// Метод, устанавливающий фигуру на доску по координатам
	public void set(int x, int y, int id) {
		set(toIndex(x, y), id);
	}

	// Метод, устанавливающий фигуру на доску по индексу
	public void set(int index, int id) {

		// Выход за границы массива
		if (!isValidIndex(index)) {
			return;
		}

		// Некорректный ID, устанавливаем значение EMPTY
		if (id < 0) {
			id = EMPTY;
		}

		// Устанавливаем биты
		for (int i = 0; i < state.length; i ++) {
			boolean set = ((1 << (state.length - i - 1)) & id) != 0;
			this.state[i] = setBit(state[i], index, set);
		}
	}

	// Метод получения значения ячейки по координатам
	public int get(int x, int y) {
		return get(toIndex(x, y));
	}

	// Метод получения значения ячейки по индексу
	public int get(int index) {
		if (!isValidIndex(index)) {
			return INVALID;
		}
		// Получение битов состояния в указанном индексе
		return getBit(state[0], index) * 4 + getBit(state[1], index) * 2
				+ getBit(state[2], index);
	}

	// Метод преобразования индекса в координаты
	public static Point toPoint(int index) {
		int y = index / 4;
		int x = 2 * (index % 4) + (y + 1) % 2;
		// Если индекс недопустим, возвращаем координаты (-1, -1)
		return !isValidIndex(index)? new Point(-1, -1) : new Point(x, y);
	}

	// Метод преобразования координат в индекс
	public static int toIndex(int x, int y) {

		// Если координаты недопустимы (не на доске или на белой клетке), возвращаем -1
		if (!isValidPoint(new Point(x, y))) {
			return -1;
		}
		
		return y * 4 + x / 2;
	}

	// Метод преобразования объекта Point в индекс
	public static int toIndex(Point p) {
		return (p == null)? -1 : toIndex(p.x, p.y);
	}

	// Метод установки бита в целевом числе
	public static int setBit(int target, int bit, boolean set) {

		// Если номер бита недопустим, ничего не делаем
		if (bit < 0 || bit > 31) {
			return target;
		}

		// Установка бита
		if (set) {
			target |= (1 << bit);
		}
		
		// Очистка бита
		else {
			target &= (~(1 << bit));
		}
		
		return target;
	}

	// Метод получения бита из целевого числа
	public static int getBit(int target, int bit) {

		// Если номер бита недопустим, возвращаем 0
		if (bit < 0 || bit > 31) {
			return 0;
		}
		
		return (target & (1 << bit)) != 0? 1 : 0;
	}

	// Метод нахождения середины отрезка между двумя точками
	public static Point middle(Point p1, Point p2) {

		// Если одна из точек не инициализирована, возвращаем координаты (-1, -1)
		if (p1 == null || p2 == null) {
			return new Point(-1, -1);
		}
		
		return middle(p1.x, p1.y, p2.x, p2.y);
	}

	// Метод нахождения середины отрезка между двумя индексами
	public static Point middle(int index1, int index2) {
		return middle(toPoint(index1), toPoint(index2));
	}

	// Метод для нахождения середины координат между двумя точками на шахматной доске
	public static Point middle(int x1, int y1, int x2, int y2) {

		// Проверка координат
		int dx = x2 - x1, dy = y2 - y1;
		if (x1 < 0 || y1 < 0 || x2 < 0 || y2 < 0 || // Не в пределах доски
				x1 > 7 || y1 > 7 || x2 > 7 || y2 > 7) {
			return new Point(-1, -1);
		} else if (x1 % 2 == y1 % 2 || x2 % 2 == y2 % 2) { // Белая клетка
			return new Point(-1, -1);
		} else if (Math.abs(dx) != Math.abs(dy) || Math.abs(dx) != 2) { // Неправильные шаги
			return new Point(-1, -1);
		}

		return new Point(x1 + dx / 2, y1 + dy / 2);
	}

	// Метод для проверки, является ли индекс действительным
	public static boolean isValidIndex(int testIndex) {
		return testIndex >= 0 && testIndex < 32;
	}

	// Метод для проверки, является ли точка действительной на шахматной доске
	public static boolean isValidPoint(Point testPoint) {

		if (testPoint == null) {
			return false;
		}

		// Проверка находится ли точка на доске
		final int x = testPoint.x, y = testPoint.y;
		if (x < 0 || x > 7 || y < 0 || y > 7) {
			return false;
		}

		// Проверка находится ли точка на черной клетке
		if (x % 2 == y % 2) {
			return false;
		}

		return true;
	}

	// Метод для получения строкового представления объекта
	@Override
	public String toString() {
		String obj = getClass().getName() + "[";
		for (int i = 0; i < 31; i ++) {
			obj += get(i) + ", ";
		}
		obj += get(31);

		return obj + "]";
	}
}
