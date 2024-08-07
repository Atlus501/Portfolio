#pragma once
//this is the object class for the tile that I'm going to put in my main Minesweeper object
#ifndef Tile_hpp
#define Tile_hpp

#include <string>
#include <iostream>

using namespace std;

class Tile {
private:
	bool mine = false;
	bool marked = false;
	bool reveal = false;
	int markedNeighbors = 0;
public:
	//constructors
	Tile() {};
	Tile(bool mined) { this->mine = mined; };


	void setMine(bool mined) { this->mine = mined; };
	void changeMark() { this->marked = !marked; };

	void setMarkedNeighbors(int nei) { 
		markedNeighbors = nei; 
	};
	bool getMarked() { return this->marked; };
	bool getMine() { return this->mine; };
	int getMarkedNeighbors() { return markedNeighbors; };

	bool getRevealed() { return reveal; };
	void setReveal(bool revealed) { reveal = revealed; };

	//reveals the tile's value when tapped on
	string showValue() {

		this->reveal = true;

		if (mine)
			return "X";
		else if (this->markedNeighbors == 0)
			return "";
		else
			return to_string(this->getMarkedNeighbors());
	};
};

#endif
