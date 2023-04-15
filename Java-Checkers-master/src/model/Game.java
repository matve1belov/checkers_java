package model;

import java.awt.Point;
import java.util.List;

import logic.MoveGenerator;
import logic.MoveLogic;

public class Game {

	private Board board; // Доска на которой происходит игра

	private boolean isP1Turn; // Показывает, кто сейчас ходит, true - если ходит первый игрок (белые)

	private int skipIndex; // Индекс, по которому был совершен прыжок, если прыжка не было, то -1

	public Game() { // Конструктор по умолчанию, создает новую доску и устанавливает ход первому игроку
		restart();
	}

	public Game(String state) { // Конструктор с параметрами, устанавливает игру в заданное состояние
		setGameState(state);
	}

	public Game(Board board, boolean isP1Turn, int skipIndex) { // Конструктор с параметрами, создает игру с заданными параметрами
		this.board = (board == null)? new Board() : board; // Создание новой доски, если переданная доска null
		this.isP1Turn = isP1Turn;
		this.skipIndex = skipIndex;
	}

	public Game copy() { // Метод, создающий копию игры
		Game g = new Game();
		g.board = board.copy();
		g.isP1Turn = isP1Turn;
		g.skipIndex = skipIndex;
		return g;
	}

	public void restart() { // Метод, перезапускающий игру
		this.board = new Board();
		this.isP1Turn = true;
		this.skipIndex = -1;
	}

	public boolean move(Point start, Point end) { // Метод, позволяющий сделать ход из точки start в точку end
		if (start == null || end == null) { // Проверка корректности введенных координат
			return false;
		}
		return move(Board.toIndex(start), Board.toIndex(end)); // Передача хода в виде индексов
	}

	public boolean move(int startIndex, int endIndex) { // Метод, совершающий ход, если он корректный

		// Validate the move
		if (!MoveLogic.isValidMove(this, startIndex, endIndex)) { // Проверка корректности хода
			return false;
		}
		
		// Make the move
		Point middle = Board.middle(startIndex, endIndex);
		int midIndex = Board.toIndex(middle);
		this.board.set(endIndex, board.get(startIndex));
		this.board.set(midIndex, Board.EMPTY);
		this.board.set(startIndex, Board.EMPTY);

		// Сделать дамку, если необходимо
		Point end = Board.toPoint(endIndex); // получаем координаты конечной позиции
		int id = board.get(endIndex); // получаем id шашки
		boolean switchTurn = false; // флаг для переключения хода
		if (end.y == 0 && id == Board.WHITE_CHECKER) { // если белая шашка дошла до верхней границы поля, то она становится белым королем
			this.board.set(endIndex, Board.WHITE_KING);
			switchTurn = true; // ход переходит к другому игроку
		} else if (end.y == 7 && id == Board.BLACK_CHECKER) { // если черная шашка дошла до нижней границы поля, то она становится черным королем
			this.board.set(endIndex, Board.BLACK_KING);
			switchTurn = true; // ход переходит к другому игроку
		}

		// Проверяем, должен ли ход перейти к другому игроку (т.е. нет больше возможности делать ходы)
		boolean midValid = Board.isValidIndex(midIndex); // проверяем, находится ли середина поля на игровом поле
		if (midValid) { // если шашка съела другую шашку, то можно продолжить ходить
			this.skipIndex = endIndex; // запоминаем индекс конечной позиции, откуда можно продолжить ход
		}
		if (!midValid || MoveGenerator.getSkips(board.copy(), endIndex).isEmpty()) { // если середина не находится на игровом поле, или нет больше возможных взятий
			switchTurn = true; // ход переходит к другому игроку
		}
		if (switchTurn) {
			this.isP1Turn = !isP1Turn; // переключаем игрока
			this.skipIndex = -1; // сбрасываем индекс, откуда можно продолжить ход
		}

		return true;
	}

	public Board getBoard() {
		return board.copy(); // возвращаем копию игрового поля
	}

	public boolean isGameOver() {

		// Убедитесь, что есть хотя бы одна шашка каждого цвета
		List<Point> black = board.find(Board.BLACK_CHECKER);
		black.addAll(board.find(Board.BLACK_KING)); // Добавляем короля, если есть
		if (black.isEmpty()) {
			return true;
		}
		List<Point> white = board.find(Board.WHITE_CHECKER);
		white.addAll(board.find(Board.WHITE_KING)); // Добавляем короля, если есть
		if (white.isEmpty()) {
			return true;
		}

// Проверяем, может ли текущий игрок сделать ход
		List<Point> test = isP1Turn? black : white;
		for (Point p : test) {
			int i = Board.toIndex(p);
			if (!MoveGenerator.getMoves(board, i).isEmpty() ||
					!MoveGenerator.getSkips(board, i).isEmpty()) { // Проверяем наличие возможных ходов и взятий
				return false;
			}
		}

// Нет возможных ходов
		return true;
	}
	
	public boolean isP1Turn() {
		return isP1Turn;
	}
	
	public void setP1Turn(boolean isP1Turn) {
		this.isP1Turn = isP1Turn;
	}
	
	public int getSkipIndex() {
		return skipIndex;
	}

	public String getGameState() {

		// Добавляем игровую доску
		String state = "";
		for (int i = 0; i < 32; i ++) {
			state += "" + board.get(i);
		}

// Добавляем другую информацию
		state += (isP1Turn? "1" : "0"); // Добавляем информацию о текущем игроке
		state += skipIndex; // Добавляем индекс пропущенной шашки, если есть

		return state;
	}

	public void setGameState(String state) {

		restart(); // Перезапускаем игру

// Простые случаи
		if (state == null || state.isEmpty()) {
			return;
		}

// Обновляем игровую доску
		int n = state.length();
		for (int i = 0; i < 32 && i < n; i ++) {
			try {
				int id = Integer.parseInt("" + state.charAt(i));
				this.board.set(i, id);
			} catch (NumberFormatException e) {}
		}

// Обновляем другую информацию
		if (n > 32) {
			this.isP1Turn = (state.charAt(32) == '1'); // Обновляем информацию о текущем игроке
		}
		if (n > 33) {
			try {
				this.skipIndex = Integer.parseInt(state.substring(33)); // Обновляем индекс пропущенной шашки, если есть
			} catch (NumberFormatException e) {
				this.skipIndex = -1;
			}
		}
	}
}
