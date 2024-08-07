#pragma once
#include <wx/wx.h>
#include <vector>
#include "Minesweeper.hpp"

class MainFrame : public wxFrame
{
public:
	MainFrame(const wxString& title);
	void setDifficulty(string difficulty);
private:
	vector<vector<wxButton*>> buttonBoard;

	//variables that track the progress of the game
	bool continueGame;
	bool genBoard;

	//functions regarding the side
	int side = 0;
	void setSide(int s) { side = s; };
	int getSide() { return side; };

	bool validIndex(int row, int col);
	void resetParameters();

	//font
	wxFont difficultyFont;

	//everything needed from the game
	Minesweeper * game;
	vector<vector<Tile>> * gameBoard;

	//the main parts of the gui
	wxPanel* mainPanel;
	void createSubFrame();
	void createWelcome();

	//the part that shows the number of mines
	void showNumberOfMines();
	wxStaticText* numberOfMines;
	void updateNumberOfMines();
	int mines=0;

	//everything required for setting up the maingame
	void setUpBoard();
	wxPanel* setUpPanel;
	wxGridSizer* mineLayout;
	wxBoxSizer* helperSizer;

	//methods that sets up the widgets
	void hardModeButton();
	void mediumModeButton();
	void easyModeButton();

	//methods that manage button presentation
	void reveal(int row, int col);
	void clearButtonBoard();
	void updateButton(int row, int col);
	void markButton(int row, int col);

	//the panel that explains the program to anyone
	bool explainPanelOpened = false;
	wxPanel* explainPanel;
	wxButton* openExplainPanel;
	void showExplainPanel(wxCommandEvent& evt);
	void createExplainPanel();
	string explainText = 
"Welcome to Minesweeper.\n \
\n\
How this game works is that the board will be filled with a lot of mines. Your goal in this game is to mark all of the tiles\
that you'd think are mined. You'd know if they're mined if the tile has an X. However, once you've revealed a mined tile, you lose the game. \
However, you could guess which tiles are mined based on the clues of the tile. But before that, here's how you start the game: \n\
\n \
After you've selected a difficulty and the board has generated, left click any tile on the board to begin the game. The 5x5 area you've\
left clicked will be guarenteed to not be mines. \n \
\n \
In this game, you reveal a grey tile by left clicking on it. It will either display a number or an X and the tile will turn cyan. The number it displays shows\
the number of mines that are surrounding the tile in a 3x3 space. That number will give you a clue on where the mines could be.\
To quickly reveal a space around a tile, you could left click on a revealed tile and it'll reveal all unmarked tiles in a 3x3 radius.\
Just make sure that all the spaces that you think are mined are marked or you could reveal a mined tile.\n \
\n\
If you're sure a space is a mine, mark it by right clicking on a tile. Once you've done so, the tile will turn yellow with a O displayed. \
If you'd like to unmark it, you could right click on said marked tile again.\
\n \
Throughout the game, you'll need to slowly reveal or mark the tiles with caution. The number of mines that are remaining will be displayed on the top \
left corner.Each time you mark a tile, the number will go down by one (and the opposite is true). Once that number hits 0 or you reveal a mined\
tile, the game will be over. Keep in mind that even if you use every one of your mines, if the any of the spaces you've marked don't have a mine\
under them, you'll still lose. So make sure to be careful when you're using your last mark.\n\
\n\
I hope this has been a helpful explaination, and I hope you'll enjoy this game!\
\n\
Click the help button again to close this window.\
";

	//methods that determine victory
	bool lose;
	wxStaticText* resultLabel;
	void showResult();

	int maxMines;
};

