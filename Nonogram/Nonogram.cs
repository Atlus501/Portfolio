using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Nonogram
{
    internal class Nonogram
    {
        private int size = 0;
        private int totalTiles = 0;
        int[,] board = null;
        int totalFilled = 0;

        public Nonogram()
        {}

        public int getColorCode(int row, int col)
        {
            return this.board[row, col];
        }

        //generates the answer board of the nonogram
        private void generateBoard()
        {
            Random rng = new Random();
            totalFilled = 0;

            int difficulty = (size - 6) / 3 + 1;

            board = new int[size, size];

            for (int row = 0; row < this.size; row++)
                for (int col = 0; col < this.size; col++)
                {

                    int generated = rng.Next(0, difficulty);
                    board[row, col] = generated;

                    if (generated != 0)
                        totalFilled += 1;

                }
        }

        public int TotalFilled
        {
            get { return this.totalFilled; }
            set { this.totalFilled = value; }
        }

        //getter and setter methods of size
        public int Size
        {
            get { return size; }
            set
            {
                this.size = value;
                generateBoard();
            }

        }

        public int[,] Board{
            get{ return board; }
            set { this.board = value; }
        }


        }
}
