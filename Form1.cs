using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using static System.Net.Mime.MediaTypeNames;

namespace Nonogram
{

    public partial class Form1 : Form
    {

        Nonogram game = new Nonogram();
        TableLayoutPanel board = new TableLayoutPanel();
        Label currentColor = null;
        int buttonsClicked = 0;
        bool beginGame = false;

        Color[] palette =
            {Color.White,
            Color.FromArgb(255, 0, 255, 0),
            Color.FromArgb(255, 0, 200, 250),
            Color.FromArgb(255, 250, 0, 200),
        };

        Color paint = Color.FromArgb(255, 0, 255, 0);

        //holder lambda methods for colorChoices
        private delegate void holderFunction(object sender, EventArgs e);
        private holderFunction hold;

        private delegate void holderMouseFunction(object sender, MouseEventArgs e);
        private holderMouseFunction holdMouse;
        private bool gameOver = false;

        Button[,] buttonBoard = null;


        //constructor method for the GUI
        public Form1()
        {
            InitializeComponent();
            this.Size = new System.Drawing.Size(950, 820);
            this.AutoScroll = true;
            this.Location = new Point(400, 0);
            this.StartPosition = FormStartPosition.Manual;
            paint = palette[0];

            //this.createButton();

            this.initialBoardSettings();

            this.createColorPanel();
            this.easyDifficulty();
            this.mediumDifficulty();
            this.hardDifficulty();

            this.createCurrentColor();
            this.createClearButton();
            this.createSurrenderButton();
            this.createHelpButton();
            this.loadGameButton();

            this.FormClosing += new FormClosingEventHandler(saveData);
        }

        private void loadGameButton() {
            Button loadGame = new Button();
            loadGame.BackColor = Color.Yellow;
            loadGame.SetBounds(620, 20, 270, 30);
            loadGame.Text = "Load Last Game";
            loadGame.Font = new Font("Arial", 14);

            loadGame.Click += new EventHandler(loadData);
            this.Controls.Add(loadGame);
        }


        private void loadData(Object sender, EventArgs e) {

            string file = "savedNonogramData.txt";
            
            try
            {
                using (StreamReader fs = new StreamReader(file))
                {
                    int size = int.Parse(fs.ReadLine());

                    beginGame = true;
                    board.BackColor = Color.Transparent;
                    board.Controls.Clear();
                    this.game.Size = size;
                    setBoardSize(size);

                    for (int row = 0; row < this.game.Size; row++)
                    {
                        for (int col = 0; col < this.game.Size; col++)
                            this.game.Board[row, col] = int.Parse(fs.ReadLine());

                        this.board.RowStyles.Add(new RowStyle(SizeType.Absolute, 30F));
                        this.board.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 30F));
                    }

                    this.interpretColors();

                    for (int row = 0; row < this.game.Size; row++)
                        for (int col = 0; col < this.game.Size; col++)
                        {

                            int sample = int.Parse(fs.ReadLine());

                            if (sample == -1)
                            {
                                this.buttonBoard[row, col].BackColor = Color.Gray;
                                continue;
                            }

                            this.buttonBoard[row, col].BackColor = palette[sample];

                        }

                    this.game.TotalFilled = int.Parse(fs.ReadLine());
                    this.buttonsClicked = int.Parse(fs.ReadLine());
                    paint = palette[1];
                    board.BackColor = Color.FromArgb(250, 250, 189, 229);

                    fs.Close();
                }
            }
            catch(FileNotFoundException) {
                MessageBox.Show("Game Not Found");
            
            }
        
        
        }

        private void saveData(Object sender, FormClosingEventArgs e)
        {
            

            if (beginGame && !gameOver)
            {
                string file = "savedNonogramData.txt";
                int size = this.game.Size;

                using (StreamWriter fs = new StreamWriter(file))
                {

                    fs.WriteLine(size);

                    for (int row = 0; row < size; row++)
                        for (int col = 0; col < size; col++)
                            fs.WriteLine(this.game.Board[row, col]);

                    for (int row = 0; row < size; row++)
                        for (int col = 0; col < size; col++)
                        {
                            fs.WriteLine(translateToColorCode(row, col));
                        }

                    fs.WriteLine(this.game.TotalFilled);
                    fs.WriteLine(this.buttonsClicked);

                    fs.Close();
                }
              
            }
           
            
        }

        private int translateToColorCode(int row, int col) {
            Color sampleColor = buttonBoard[row, col].BackColor;

            if (sampleColor == palette[0])
                return 0;
            if (sampleColor == palette[1])
                return 1;
            if (sampleColor == palette[2])
                return 2;
            if (sampleColor == palette[3])
                return 3;

            return -1;
        }

        private void createHelpButton()
        {
            Button help = new Button();
            help.BackColor = Color.Orange;
            help.Text = "Help";
            help.SetBounds(20, 300, 60, 30);
            help.Font = new Font("Arial", 14);
            help.TextAlign = ContentAlignment.MiddleCenter;
            help.Click += new EventHandler(helpBox);

            this.Controls.Add(help);
        }

        private void helpBox(Object sender, EventArgs e) {
            string helpfulInstruction = "How you win this game is that you must fill in the colors based on the given clues. \n" +
                "The numbers with the black blackground tell you how many buttons in the row/column have the same color that row/column " +
                "(the color of the number tells you what colors are in a row.\n" +
                "For instance, if there are green numbers in the buttons on row 1 that says 1 6 and one blue number that says 5, it means that in row 1, " +
                "there will be 1 green buttons in a row, 6 green buttons in a row, 5 blue buttons in a row.\n" +
                "Once you created the right pattern, you will win the game."+
                "You can adjust the difficulty using the three buttons that says easy (9x9 and 1 color), medium (12x12 and 2 colors), and hard" +
                "(15x15 and 3 colors). Press on one of those buttons to start the game.\n" +
                "Please be patient. The game might take a bit of time to load.\n" +
                "For starters, you can see the color your mouse has via the current color label and you can change your color by pressing on" +
                "one of the three color swatches on the top left corner of the screen.\n"+
                "When you left click on a white or gray button, it turns to your current color. Leftclicking on a colored button will turn it white." +
                "RightClicking on a white or colored button will mark it gray (functionally, it is the same as a white button; but, it is useful to mark places" +
                "where the tile must be white.\n" +
                "If you left click in one of the black buttons, it will turn every white button in that respective row/column to your current color.\n" +
                "If you right click one of the black buttons, it will turn every white button gray. in that respective row/column gray.\n" +
                "I hope this is helpful information and enjoy your game!";


            MessageBox.Show(helpfulInstruction);
        }

        private void createSurrenderButton() {
            Button surrender = new Button();
            surrender.SetBounds(20, 250, 105, 40);
            surrender.Text = "Surrender";
            surrender.BackColor = Color.Red;
            surrender.Font = new Font("Arial", 14);

            surrender.Click += new EventHandler(surrenderGame);
            surrender.TextAlign = ContentAlignment.MiddleCenter;
            this.Controls.Add(surrender);
        
        }

        private void surrenderGame(Object sender, EventArgs e)
        {
            if (beginGame)
            {
                int limit = this.game.Size;

                for (int row = 0; row < limit; row++)
                    for (int col = 0; col < limit; col++)
                        buttonBoard[row, col].BackColor = palette[this.game.Board[row, col]];

                MessageBox.Show("How Unfortunate");
                this.gameOver = true;
                beginGame = false;
            }
        }

        private void createCurrentColor() {
            paint = palette[1];

            currentColor = new Label();
            currentColor.SetBounds(20, 150, 85, 50);
            currentColor.BackColor = Color.Black;
            currentColor.Text = "Current Color";
            currentColor.Font = new Font("Arial", 14);
            currentColor.ForeColor = paint;

            this.Controls.Add(currentColor);
        }

        private void createClearButton() {
            Button clear = new Button();
            clear.SetBounds(20, 210, 90, 30);
            clear.Text = "Restart";
            clear.Font = new Font("Arial", 14);

            clear.Click += new EventHandler(clearBoard);

            clear.TextAlign = ContentAlignment.MiddleCenter;


            this.Controls.Add(clear);
        }

        private void clearBoard(Object sender, EventArgs e) {

            this.gameOver = false;
            this.beginGame = true;

            int limit = this.game.Size;

            for (int row = 0; row < limit; row++)
                for (int col = 0; col < limit; col++)
                    buttonBoard[row, col].BackColor = Color.White;

            this.buttonsClicked = 0;
        }

        //creates the panel for the color switches
        private void createColorPanel() {
            for (int index = 1; index < palette.Length; index++)
                this.colorChoices(index);
        }

        private void interpretColors() {

            int boardLength = board.RowCount;
            buttonsClicked = 0;
            gameOver = false;

            buttonBoard = new Button[boardLength, boardLength];

            for (int row = 0; row < boardLength; row++)
            {
                for (int col = 0; col < boardLength; col++)
                {
                    Button added = new Button();
                    added.BackColor = Color.White;
                    buttonBoard[row, col] = added;

                    added.MouseDown += new MouseEventHandler(shiftColor);

                    added.Dock = DockStyle.Fill;

                    int code = this.game.getColorCode(row, col);
                    //added.BackColor = Color.White;

                    //if (code <= (boardLength - 6) / 3)
                    // added.BackColor = palette[code];
                    this.board.Controls.Add(added, col, row);
                }

                this.setRowLabel(row);
            }
            for (int col = 0; col < boardLength; col++)
                this.setColLabel(col);
        }

        //creates the easy mode button
        private void easyDifficulty() {
            Button easyMode = new Button();

            easyMode.SetBounds(200, 60, 80, 40);
            easyMode.Text = "Easy";
            easyMode.BackColor = palette[1];
            easyMode.Font = new Font("Arial", 15);

            hold = (object sender, EventArgs e) => { setDifficulty(9); };

            easyMode.Click += new EventHandler(hold);

            this.Controls.Add(easyMode);
        }

        //creates the medium mode difficulty button
        private void mediumDifficulty() {
            Button mediumMode = new Button();

            mediumMode.SetBounds(350, 60, 100, 40);
            mediumMode.Text = "Medium";
            mediumMode.BackColor = palette[2];
            mediumMode.Font = new Font("Arial", 15);

            hold = (object sender, EventArgs e) => { setDifficulty(12); };

            mediumMode.Click += new EventHandler(hold);

            this.Controls.Add(mediumMode);
        }

        private void hardDifficulty()
        {
            Button hardMode = new Button();

            hardMode.SetBounds(520, 60, 80, 40);
            hardMode.Text = "Hard";
            hardMode.BackColor = palette[3];
            hardMode.Font = new Font("Arial", 15);

            hold = (object sender, EventArgs e) => { setDifficulty(15); };

            hardMode.Click += new EventHandler(hold);

            this.Controls.Add(hardMode);
        }

        //helper method for setting up the difficulty of the game
        private void setDifficulty(int size) {

            beginGame = true;
            board.BackColor = Color.Transparent;
            board.Controls.Clear();
            this.game.Size = size;
            setBoardSize(size);

            for (int index = 0; index < size + 1; index++)
            {
                this.board.RowStyles.Add(new RowStyle(SizeType.Absolute, 30F));
                this.board.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 30F));
            }

            this.interpretColors();
            paint = palette[1];
            board.BackColor = Color.FromArgb(250, 250, 189, 229);
        }

        //creates the labels for the rows
        private void setRowLabel(int row) {
            int maxSize = this.game.Size;
            int[,] sampleBoard = this.game.Board;
            int savedColor = -1;
            int streak = 0;

            List<int> data = new List<int>();

            for (int col = 0; col < maxSize; col++)
            {
                int colorSample = sampleBoard[row, col];


                if (col == maxSize - 1 && colorSample > 0)
                {

                    if (colorSample != savedColor && savedColor > 0)
                    {
                        data.Add(streak);
                        data.Add(savedColor);

                        data.Add(1);
                        data.Add(colorSample);

                    }
                    else
                    {
                        data.Add(streak + 1);
                        data.Add(colorSample);
                    }
                }
                else if (!(colorSample == savedColor && savedColor > 0))
                {
                    if (savedColor > 0 && streak > 0)
                    {
                        data.Add(streak);
                        data.Add(savedColor);
                    }

                    savedColor = colorSample;

                    if (savedColor > 0)
                        streak = 1;
                    else
                        streak = 0;
                }
                else
                {
                    streak++;

                }
            }

            if (board.ColumnCount < maxSize + data.Count / 2)
            {
                this.board.ColumnCount = maxSize + data.Count / 2;

                for (int col = 0; col < data.Count / 2; col++)
                {
                    // this.board.RowStyles.Add(new RowStyle(SizeType.Absolute, 30F));
                    this.board.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 30F));
                    this.board.RowStyles.Add(new RowStyle(SizeType.Absolute, 30F));
                }
            }

            for (int column = 0; column < data.Count; column += 2) {

                Button addedData = new Button();
                addedData.TextAlign = ContentAlignment.MiddleCenter;
                addedData.Text = data[column].ToString();
                addedData.BackColor = Color.Black;
                addedData.ForeColor = palette[data[column + 1]];
                addedData.Font = new Font("Arial", 10);
                addedData.Dock = DockStyle.Fill;

                holdMouse = (object sender, MouseEventArgs e) => { this.fillRow(row, e); };

                addedData.MouseDown += new MouseEventHandler(holdMouse);

                this.board.Controls.Add(addedData, maxSize + column / 2, row);
            }

        }

        private void setColLabel(int col)
        {
            int maxSize = this.game.Size;
            int[,] sampleBoard = this.game.Board;
            int savedColor = -1;
            int streak = 0;

            List<int> data = new List<int>();

            for (int row = 0; row < maxSize; row++)
            {
                int colorSample = sampleBoard[row, col];


                if (row == maxSize - 1 && colorSample > 0)
                {

                    if (colorSample != savedColor && savedColor > 0)
                    {
                        data.Add(streak);
                        data.Add(savedColor);

                        data.Add(1);
                        data.Add(colorSample);

                    }
                    else
                    {
                        data.Add(streak + 1);
                        data.Add(colorSample);
                    }
                }
                else if (!(colorSample == savedColor && savedColor > 0))
                {
                    if (savedColor > 0 && streak > 0)
                    {
                        data.Add(streak);
                        data.Add(savedColor);
                    }

                    savedColor = colorSample;

                    if (savedColor > 0)
                        streak = 1;
                    else
                        streak = 0;
                }
                else
                {
                    streak++;

                }
            }

            if (board.RowCount < maxSize + data.Count / 2)
            {
                this.board.RowCount = maxSize + data.Count / 2;

                for (int row = 0; row < data.Count / 2; row++)
                {
                    // this.board.RowStyles.Add(new RowStyle(SizeType.Absolute, 30F));
                    this.board.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 30F));
                    this.board.RowStyles.Add(new RowStyle(SizeType.Absolute, 30F));
                }
            }

            for (int row = 0; row < data.Count; row += 2)
            {
                Button addedData = new Button();
                addedData.TextAlign = ContentAlignment.MiddleCenter;
                addedData.Text = data[row].ToString();
                addedData.BackColor = Color.Black;
                addedData.ForeColor = palette[data[row + 1]];
                addedData.Font = new Font("Arial", 10);
                addedData.Dock = DockStyle.Fill;

                holdMouse = (object sender, MouseEventArgs e) => { this.fillCol(col, e); };

                addedData.MouseDown += new MouseEventHandler(holdMouse);

                this.board.Controls.Add(addedData, col, maxSize + row / 2);
            }

        }

        private void fillRow(int row, MouseEventArgs m) {
            for (int col = 0; col < this.game.Size; col++)
            {
                fillHelper(row, col, m);
            }
        }

        private void fillCol(int col, MouseEventArgs m)
        {
            for (int row = 0; row < this.game.Size; row++) {
                fillHelper(row, col, m);

            }

        }

        private void fillHelper(int row, int col, MouseEventArgs m) {
            Button sampleButton = buttonBoard[row, col];

            if (sampleButton.BackColor == Color.White) {

                if (m.Button == MouseButtons.Left)
                {
                    sampleButton.BackColor = paint;
                    buttonsClicked++;
                }
                else if(m.Button == MouseButtons.Right)
                {
                    sampleButton.BackColor = Color.Gray;
                    Console.Write("Active");
                }


            } }


        //creates and places the button that determines the color
        private void colorChoices(int displacement) {

            Button colorChoice = new Button();
            colorChoice.SetBounds(30, 30 + (displacement - 1) * 40, 30, 30);
            colorChoice.BackColor = palette[displacement];

            hold = (object sender, EventArgs e) => { setColorChoice(displacement); };

            colorChoice.Click += new EventHandler(hold);

            this.Controls.Add(colorChoice);
        }

        private void setColorChoice(int index) {
            paint = palette[index];
            currentColor.ForeColor = paint;

        }

        //sets the initial board settings 
        private void initialBoardSettings() {
            board.SetBounds(130, 120, 694, 589);
            board.AutoSize = true;
            board.AutoSizeMode = AutoSizeMode.GrowAndShrink;
            this.Controls.Add(board);
        }

        //sets the size of the board 
        private void setBoardSize(int size) {
            
            board.RowCount = size;
            board.ColumnCount = size;
        }

        private void label1_Click(object sender, EventArgs e){} 

        //creatse the testing button
        private void createButton()
        {
            Button test = new Button();
            test.Text = "";
            test.MouseDown += new MouseEventHandler(shiftColor);
            test.SetBounds(200, 100, 30, 30);

            this.Controls.Add(test);
        }

        //method that shifts the color of the button when it's pressed
        private void shiftColor(object sender, MouseEventArgs e) {

            Button activeButton = sender as Button;
            Color buttonColor = activeButton.BackColor;

            if (!gameOver)
            {

                if (activeButton == null) { return; }

                if (e.Button == MouseButtons.Right)
                {

                    if (buttonColor != Color.White && buttonColor != Color.Gray)
                        buttonsClicked--;

                    if (buttonColor == Color.Gray)
                    {
                        activeButton.BackColor = Color.White;
                        return;
                    }

                    activeButton.BackColor = Color.Gray;
                    return;
                }

                if (buttonColor != Color.White && buttonColor != Color.Gray)
                {
                    activeButton.BackColor = Color.White;
                    buttonsClicked--;
                }
                else
                {
                   
                    buttonsClicked++;

                    activeButton.BackColor = paint;

                }

                if (buttonsClicked == this.game.TotalFilled)
                {
                    this.checkBoard();
                }
            }

        }

        private void checkBoard() {

            for (int row = 0; row < this.game.Size; row++)
                for (int col = 0; col < this.game.Size; col++) {

                    int sample = this.game.Board[row,col];
                    Color buttonColor = buttonBoard[row, col].BackColor;

                    if (checkingLogic(sample, buttonColor)) { return; }
                        
                }
            gameOver = true;
            beginGame = false;
            MessageBox.Show("You Won!");
        }

        private bool checkingLogic(int sample, Color buttonColor) {
            return !(sample == 0 && (buttonColor == Color.White || buttonColor == Color.Gray)) && palette[sample] != buttonColor;
        }

        private void Form1_Load(object sender, EventArgs e){ }
    }
}
