package logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import model.Board;

public class MoveGenerator {

	// Возвращает список возможных ходов из данной точки на доске
	public static List<Point> getMoves(Board board, Point start) {
		return getMoves(board, Board.toIndex(start));
	}

	// Возвращает список возможных ходов из данного индекса на доске
	public static List<Point> getMoves(Board board, int startIndex) {

		// Тривиальные случаи
		List<Point> endPoints = new ArrayList<>();
		if (board == null || !Board.isValidIndex(startIndex)) {
			return endPoints;
		}

		// Определяем возможные точки
		int id = board.get(startIndex);
		Point p = Board.toPoint(startIndex);
		addPoints(endPoints, p, id, 1);

		// Удаляем невалидные точки
		for (int i = 0; i < endPoints.size(); i ++) {
			Point end = endPoints.get(i);
			if (board.get(end.x, end.y) != Board.EMPTY) {
				endPoints.remove(i --);
			}
		}

		return endPoints;
	}


	// Возвращает список возможных прыжков из данной точки на доске
	public static List<Point> getSkips(Board board, Point start) {
		return getSkips(board, Board.toIndex(start));
	}

	// Возвращает список возможных прыжков из данного индекса на доске
	public static List<Point> getSkips(Board board, int startIndex) {

		// Тривиальные случаи
		List<Point> endPoints = new ArrayList<>();
		if (board == null || !Board.isValidIndex(startIndex)) {
			return endPoints;
		}

		// Определяем возможные точки
		int id = board.get(startIndex);
		Point p = Board.toPoint(startIndex);
		addPoints(endPoints, p, id, 2);

		// Удаляем невалидные точки
		for (int i = 0; i < endPoints.size(); i ++) {

			// Проверяем, что прыжок валидный
			Point end = endPoints.get(i);
			if (!isValidSkip(board, startIndex, Board.toIndex(end))) {
				endPoints.remove(i --);
			}
		}

		return endPoints;
	}


	// Проверяет, что данный прыжок на доске валиден
	public static boolean isValidSkip(Board board, int startIndex, int endIndex) {

		if (board == null) {
			return false;
		}

		// Проверяем, что конечная точка пустая
		if (board.get(endIndex) != Board.EMPTY) {
			return false;
		}

		// Проверяем, что середина прыжка занята противником
		int id = board.get(startIndex);
		int midID = board.get(Board.toIndex(Board.middle(startIndex, endIndex)));
		if (id == Board.INVALID || id == Board.EMPTY) {
			return false;
		} else if (midID == Board.INVALID || midID == Board.EMPTY) {
			return false;
		} else if ((midID == Board.BLACK_CHECKER || midID == Board.BLACK_KING)
				^ (id == Board.WHITE_CHECKER || id == Board.WHITE_KING)) {
			return false;
		}
		
		return true;
	}
	

	public static void addPoints(List<Point> points, Point p, int id, int delta) {
		
		// Add points moving down
		boolean isKing = (id == Board.BLACK_KING || id == Board.WHITE_KING);
		if (isKing || id == Board.BLACK_CHECKER) {
			points.add(new Point(p.x + delta, p.y + delta));
			points.add(new Point(p.x - delta, p.y + delta));
		}
		
		// Add points moving up
		if (isKing || id == Board.WHITE_CHECKER) {
			points.add(new Point(p.x + delta, p.y - delta));
			points.add(new Point(p.x - delta, p.y - delta));
		}
	}
}
