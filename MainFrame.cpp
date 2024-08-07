//the class for the main GUI for the Minesweeper game

#include "MainFrame.h"
#include <functional>
#include <wx/wx.h>

#include "Minesweeper.hpp"

MainFrame::MainFrame(const wxString& title) :wxFrame(nullptr, wxID_ANY, title) {

	//creates the main panel of the frame
	wxPanel* panel1 = new wxPanel(this);
	mainPanel = panel1;

	createSubFrame();
	createWelcome();

	game = new Minesweeper();
	resetParameters();

	//font that sets the font for the button that sets the diffiuclty of the game
	difficultyFont = *wxNORMAL_FONT;
	difficultyFont.SetPointSize(13);

	showNumberOfMines();
	showResult();

	createExplainPanel();

	easyModeButton();
	mediumModeButton();
	hardModeButton();

	mainPanel->Layout();
};

//resets the necessary parameters 
void MainFrame::resetParameters() {
	continueGame = true;
	genBoard = false;
	lose = false;

	if (resultLabel != NULL)
		resultLabel->SetLabel("");
};

//creates the subframe that all of the buttons will be placed on
void MainFrame::createSubFrame() {
	setUpPanel = new wxPanel(mainPanel, wxID_ANY, wxPoint(90, 170), wxSize(650, 650));
	setUpPanel->SetMaxSize(wxSize(650, 650));
	setUpPanel->SetBackgroundColour(wxColour(100, 0, 250));
	helperSizer = new wxBoxSizer(wxVERTICAL);
	mineLayout = new wxGridSizer(10, 10, 0, 0);
};

//the text that shows whether the player won or not
void MainFrame::showResult() {
	resultLabel = new wxStaticText(mainPanel, wxID_ANY, "", wxPoint(370, 130), wxDefaultSize, wxALIGN_CENTER);
	wxFont resultFont = *wxNORMAL_FONT;
	resultFont.SetPointSize(15);
	resultLabel->SetFont(resultFont);
};

//creates the welcome text
void MainFrame::createWelcome() {
	wxFont welcomeFont = *wxNORMAL_FONT;
	welcomeFont.SetPointSize(20);
	wxStaticText* welcome = new wxStaticText(mainPanel, wxID_ANY, "Welcome to Minesweeper", wxPoint(270, 20));
	welcome->SetFont(welcomeFont);

	//text and font that further informs the player
	wxFont explainFont = *wxNORMAL_FONT;
	explainFont.SetPointSize(10);
	wxStaticText* explain = new wxStaticText(mainPanel, wxID_ANY, "Please select a difficulty to begin!", wxPoint(320, 60));
	explain->SetFont(explainFont);
};

//creates the panel that explains the game for the player
void MainFrame::createExplainPanel() {
	explainPanel = new wxPanel(mainPanel, wxID_ANY, wxPoint(80, 150), wxSize(670, 600));

	explainPanel->SetBackgroundColour(wxColour(0, 250, 0));

	wxBoxSizer* sizer = new wxBoxSizer(wxVERTICAL);

	auto openOrNot = [this](wxCommandEvent& evt) {return MainFrame::showExplainPanel(evt);};

	openExplainPanel = new wxButton(mainPanel, wxID_ANY, "Help", wxPoint(670, 40), wxSize(60, 40));
	wxFont explainFont = *wxNORMAL_FONT;
	explainFont.SetPointSize(18);
	openExplainPanel->SetFont(explainFont);
	openExplainPanel->SetBackgroundColour(wxColour(250, 250, 0));

	openExplainPanel->Bind(wxEVT_BUTTON, openOrNot);
	
	wxStaticText* explainText = new wxStaticText(explainPanel, wxID_ANY, string(this->explainText), wxPoint(40, 20), wxSize(650, 520), wxALIGN_CENTER);
	wxFont explainTextFont = *wxNORMAL_FONT;
	explainTextFont.SetPointSize(10);
	explainText->SetFont(explainTextFont);
	sizer->Add(explainText);
	explainPanel->SetSizer(sizer);

	explainPanel->Hide();

	explainPanel->Layout();
};

void MainFrame::showExplainPanel(wxCommandEvent& evt) {
	if (explainPanelOpened) {
		explainPanel->Hide();
		this->explainPanelOpened = false;
	}
	else {
		explainPanel->Show();
		this->explainPanelOpened = true;
	}
};

//the button that sets up the easy mode
void MainFrame::easyModeButton() {
	auto easy = [this](wxCommandEvent& evt) {return MainFrame::setDifficulty("easy"); };
	wxButton* easyMode = new wxButton(mainPanel, wxID_ANY, "Easy Mode", wxPoint(180, 90), wxSize(100, 30));
	easyMode->Bind(wxEVT_BUTTON, easy);
	easyMode->SetFont(difficultyFont);
	easyMode->SetBackgroundColour(wxColour(0, 150, 205));
};

//the button that sets up the medium difficulty of the game
void MainFrame::mediumModeButton() {
	auto medium = [this](wxCommandEvent& evt) {return MainFrame::setDifficulty("medium"); };
	wxButton* mediumMode = new wxButton(mainPanel, wxID_ANY, "Medium Mode", wxPoint(340, 90), wxSize(140, 30));
	mediumMode->Bind(wxEVT_BUTTON, medium);
	mediumMode->SetFont(difficultyFont);
	mediumMode->SetBackgroundColour(wxColour(0, 250, 0));
};

//the button that sets up the hard difficulty of the game
void MainFrame::hardModeButton() {
	auto hard = [this](wxCommandEvent& evt) {return MainFrame::setDifficulty("hard"); };
	wxButton* hardMode = new wxButton(mainPanel, wxID_ANY, "Hard Mode", wxPoint(540, 90), wxSize(110, 30));
	hardMode->Bind(wxEVT_BUTTON, hard);
	hardMode->SetFont(difficultyFont);
	hardMode->SetBackgroundColour(wxColour(255, 0, 120));
};

//the function sets up the difficulty
void MainFrame::setDifficulty(string difficulty) {
	MainFrame::resetParameters();
	MainFrame::clearButtonBoard();

	setUpPanel->DestroyChildren();
	game->setDifficulty(difficulty);
	
	gameBoard = game->getBoard();

	MainFrame::setUpBoard();
	maxMines = game->getMaxMines();
};

//the static text that shows the number of mines
void MainFrame::showNumberOfMines() {
	numberOfMines = new wxStaticText(mainPanel, wxID_ANY, "", wxPoint(20, 45));
	wxFont mineText = *wxNORMAL_FONT;
	mineText.SetPointSize(12);
	numberOfMines->SetFont(mineText);
};

//this updates the static text that shows the number of mines
void MainFrame::updateNumberOfMines() {
	numberOfMines->SetLabel("Number Of Mines Remining: " + to_string(mines));

	if (mines <= 0) {
		continueGame = false;
		lose = game->checkGameOver();
	}

	if (!continueGame) {
		if (lose)
			resultLabel->SetLabel("You Lost!");
		else
			resultLabel->SetLabel("You Win!");
	}
};

//this function generates the game
void MainFrame::setUpBoard() {

	MainFrame::setSide(game->getSide());
	int side = MainFrame::getSide();

	mineLayout->SetRows(side);
	mineLayout->SetCols(side);

	for (int row = 0; row < side; row++) {
		vector<wxButton*> insert;
		for (int col = 0; col < side; col++) {
			wxButton* Tile = new wxButton(setUpPanel, wxID_ANY);

			auto reveal = [this, row, col](wxMouseEvent& evt) {return MainFrame::reveal(row, col); };
			auto mark = [this, row, col](wxMouseEvent& evt) {return MainFrame::markButton(row, col); };

			Tile->Bind(wxEVT_LEFT_DOWN, reveal);
			Tile->Bind(wxEVT_RIGHT_DOWN, mark);

			Tile->SetBackgroundColour(wxColour(200, 250, 250));
			mineLayout->Add(Tile, 1, wxEXPAND);

			insert.push_back(Tile);
		}
		buttonBoard.push_back(insert);
	}
	setUpPanel->SetSizerAndFit(mineLayout);
	setUpPanel->Layout();

	mines = game->getMaxMines();
	MainFrame::updateNumberOfMines();
};

//updates the mark of the button if the user right clicks
void MainFrame::markButton(int row, int col) {

	if (genBoard && continueGame && !game->getReveal(row, col)) {
		game->mark(row, col);

		wxButton* button = buttonBoard[row][col];

		if (game->getMark(row, col)) {
			button->SetBackgroundColour(wxColour(250, 250, 0));
			button->SetLabel("O");
			mines -= 1;
			updateNumberOfMines();
		}
		else {
			button->SetBackgroundColour(wxColour(200, 250, 250));
			button->SetLabel("");
			mines += 1;
			updateNumberOfMines();
		}
	}
};

//updates the button 
void MainFrame::updateButton(int row, int col) {

	wxButton* button = buttonBoard[row][col];
	string label = game->reveal(row, col);

	button->SetBackgroundColour(wxColour(0, 250, 250));
	button->SetLabel(label);
	game->setReveal(row, col);
};

//reveals the tile at the spot of the board
void MainFrame::reveal(int row, int col) {

	if (!genBoard) {
		game->setBoard(row, col);
		genBoard = true;
	}

	if (continueGame && !game->getMark(row, col)) {

		if(!game->getReveal(row, col)){
			string label = game->reveal(row, col);
			MainFrame::updateButton(row, col);

			if (label == "X") {
				continueGame = false;
				lose = true;
				resultLabel->SetLabel("You Lose!");
			}
			
			if (label == "") {
				for (int r = row - 1; r <= row + 1 && r < side; r++) {

					if(r >= 0)
						for (int c = col - 1; c <= col + 1 && c < side; c++) {
							if (c >= 0 && !game->getReveal(r, c) && !game->getMark(r,c)) 
								MainFrame::reveal(r, c);
						}
				}
			}
		}
		else{
			for (int r = row - 1; r <= row + 1 && r < side; r++) {

				if(r>=0)
					for (int c = col - 1; c <= col + 1 && c < side; c++) {

						if (c >= 0 && !game->getReveal(r, c)) {

							bool emptyTile = game->reveal(r, c) == "";

							if (!game->getMark(r, c)) {

								if (emptyTile) {
									MainFrame::updateButton(r, c);
									MainFrame::reveal(r, c);
								}
								else
									MainFrame::updateButton(r, c);

								if (game->reveal(r, c) == "X") {
									continueGame = false;
									resultLabel->SetLabel("You Lose!");
								}
							}
						}
					}
			}
		}
	}
};

//returns if the index is on the board
bool MainFrame::validIndex(int row, int col) {
	int side = MainFrame::getSide();
	return row >= 0 && row < side && col >= 0 && col < side;
};

//clears up the button board
void MainFrame::clearButtonBoard() {

		for (int index = 0; index < buttonBoard.size(); index++) {

			buttonBoard[index].clear();
			buttonBoard[index].shrink_to_fit();
		}
		buttonBoard.clear();
		buttonBoard.shrink_to_fit();
};