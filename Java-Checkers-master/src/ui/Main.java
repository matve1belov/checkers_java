package ui;

import javax.swing.UIManager;

public class Main {

	public static void main(String[] args) {

		// Установка системного стиля оформления для приложения
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Создание окна для отображения игры в шашки
		CheckersWindow window = new CheckersWindow();
		// Назначение действия на кнопку закрытия окна
		window.setDefaultCloseOperation(CheckersWindow.EXIT_ON_CLOSE);
		// Отображение окна
		window.setVisible(true);
	}
}
