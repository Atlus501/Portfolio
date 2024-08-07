#pragma once

//this is the object file for my minesweeper class
#ifndef Minesweeper_h
#define Minesweeper_h

#include "Tile.hpp"

#include <vector>
#include <random>
#include <cstdlib>
#include <time.h>
#include <math.h>

class Minesweeper {

private:
	int side = 0;
	vector<vector<Tile>> board;
	int maxMines = 0;
	double chanceOfMarked = 0.0;
	int reminingMines = 0;

public:
	//constructor if the minesweeper object
	Minesweeper() {
	};

	//returns the number of remaining mines
	int getMaxMines() {
		return maxMines;
	};

	//returns the side of the board
	int getSide() {
		return side;
	};

	//checks if the game is over (if the tiles are marked or revealed)
	bool checkGameOver() {
	
		for (int row = 0; row < side; row++)
			for (int col = 0; col < side; col++)
				if (board[row][col].getMarked() != board[row][col].getMine())
					return true;

		return false;
	}

	//sets the difficulty and then sets the board
	void setDifficulty(string difficulty) {

		this->clearBoard();

		if (difficulty == "easy") {
			side = 9;
			maxMines = 10;
		}
		else if (difficulty == "medium") {
			side = 18;
			maxMines = 40;
		}
		else {
			side = 24;
			maxMines = 99;
		}
	}

	bool getMark(int row, int col) {
		return board[row][col].getMarked();
	};

	//first clears up the board, then sets up the board with the mines
	void setBoard(int startRow, int startCol) {

		clearBoard();

		int numberMined = 0;

		for (int row = 0; row < side; row++) {
			vector<Tile> addedRow;

			for (int col = 0; col < side; col++) {

				Tile added = Tile();
				
				if (mineLogic(numberMined, startRow, startCol, row, col)) {

					added.setMine(true);
					numberMined++;
				}
				addedRow.push_back(added);
			}
			board.push_back(addedRow);
		}

		if (numberMined < maxMines)
			this->correctMines(numberMined, startRow, startCol);


		this->markNeighbors();
	};

	void correctMines(int numberMined, int startRow, int startCol) {

		int mines = numberMined;

		if (mines < maxMines) {
			for (int row = 0; row < side; row++)
				for (int col = 0; col < side; col++) {
					if (mineLogic(mines, startRow, startCol, row, col) && !board[row][col].getMine()) {
						mines++;
						board[row][col].setMine(true);
					}

					if (row == side - 1 && col == side - 1 && mines < maxMines)
						correctMines(mines, startRow, startCol);
				}
		}
	};

	//the logic used to determine if the space is going to be mined
	bool mineLogic(int numberMined, int startRow, int startCol, int row, int col) {

		std::random_device rd;  // Obtain a random number from hardware
		std::mt19937 gen(rd()); // Seed the generator
		std::uniform_int_distribution<> distr(0, side*side);

		//or the number of mines is less, that spot is mined
		return (numberMined < maxMines) &&

			//gets a number that is smaller than the number of maxmines
			((distr(gen) % (side * side) <= maxMines) ||

			//if the number of mines is less than the marked mines, or if the random gen
			(side * side - ((row) * (side)+(col + 1)) <= numberMined)) &&

			//makes sure that the 5x5 area around the starting point doesn't have mines to 
			//make the game fair to play
			!(abs(row - startRow) < 3 && abs(col - startCol) < 3);
	}

	//checks if the index fits in the board
	bool validIndex(int row, int col) {
		return row >= 0 && row < side && col >= 0 && col < side;
	};

	//clears up the board in order to make a new one
	//swaps out the original vectors for empty ones in order to mitigate memory issues
	void clearBoard() {
		for (int index = 0; index < board.size(); index++) {
		
			board[index].clear();
			board[index].shrink_to_fit();
		}
		board.clear();
		board.shrink_to_fit();
	}

	//sets up the number of nieghbors in each tile
	void markNeighbors() {

		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {

				int neighbors = 0;

				Tile sampleTile = board[row][col];

				if (!sampleTile.getMine()) {

					for (int r = row - 1; r <= row + 1; r++)
						for (int c = col - 1; c <= col + 1; c++) {
							if (neighborDeterminingLogic(r, c, row, col) ) { //&& (r != row || c != col)
								neighbors++;
							}
						}
					board[row][col].setMarkedNeighbors(neighbors);
				}
			}
		}

	};

	//the logic that determines if the number of neighbors are increased
	bool neighborDeterminingLogic(int r, int c, int row, int col) {
		return validIndex(r, c) && (r != row || c != col) && board[r][c].getMine();
	}

	//marks or unmarks the tile if the user think its mined
	void mark(int row, int col) {
		board[row][col].changeMark();
	}

	//reveals if the tile is marked or the number of its 
	string reveal(int row, int col) {
		string sample = board[row][col].showValue();
		return sample;
	};

	void setReveal(int row, int col) {
		board[row][col].setReveal(true);
	};

	vector<vector<Tile>>* getBoard() {
		return &board;
	}

	//test the board generation in the console
	void testBoard() {

		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				cout << board[row][col].showValue() << " ";
			}
			cout << "\n";
		}
	};

	bool getReveal(int row, int col) {
		return board[row][col].getRevealed();
	};

};
#endif

