import tkinter as tk
from tkinter import messagebox
from commandprompt import *
from functools import *
from datetime import *

class MyGUI: 
 
 root = NULL
 masterframe = NULL
 listing = NULL
 memory = NULL
 green = '#7DDA58'
 cyan = '#66ffff'
 orange = '#CC6CE7'
 
 def __init__(self, list0, list1):
  self.root = tk.Tk()
  self.listing = list0
  self.memory = list1

  self.root.geometry("800x800")
  
  self.label = tk.Label(self.root, text="Welcome to My Reminders", font=('Arial', 18))
  self.label.pack(pady = 10, padx=10)
  
  self.clearButton = tk.Button(self.root, text = "Clear", font = ('Arial', 10), command = self.clear, bg = self.green)
  self.clearButton.place(x = 150, y = 50, height = 30, width = 40)
  
  self.undoButton = tk.Button(self.root, text = "Undo", font = ('Arial', 10), command = self.undo, bg = self.green)
  self.undoButton.place(x = 700, y =50, height = 30, width = 40)
  
  self.sortButton = tk.Button(self.root, text = "Sort", font = ('Arial', 10), command = self.sort, bg = self.green)
  self.sortButton.place(x = 600, y = 50, height = 30, width = 40)
  
  self.help() 
  self.addingEntry()
  self.showList()

  self.root.protocol("WN_DELETE_WINDOW", self.closing) #closing protocol

  self.root.mainloop()

 def clear(self): #clears the list
  analyse("clear", self.listing, self.memory)
  self.update()

 def sort(self): #sorts the list
  analyse("sort", self.listing, self.memory)
  self.update()
  
 def undo(self): #undo the last change of the list
  analyse("undo", self.listing, self.memory)
  self.update()

 def closing(self): #closing command
  self.root.destroy() 
  self.finish = True

 def addingEntry(self): #entry that adds a reminder

  added = tk.StringVar()
  self.addedReminder = tk.Entry(self.root, font=('Arial', 12), textvariable = added)
  
  def add(inner):#command that adds the new reminderss
   analyse("add "+added.get(), self.listing, self.memory)
   added.set("")
   self.update()

  self.addedReminder.bind("<Return>", add)
  self.addedReminder.pack(pady=5)

 def help(self): #help button instructions
  self.needHelp = tk.BooleanVar()
  
  self.helpButton = tk.Button(self.root, text = "?", font = ('Arial', 10), background = self.green, command = self.showHelp)
  self.helpButton.place(height = 30, width = 30, x = 50, y =50) 
  
 def showHelp(self): #help button window
  helpText = "Hello there. Welcome to my reminders app. \n \
  As you've probably figured, the buttons on the top panel do exactly what they say. \n \
  In order to add a reminder to the list, simply enter the reminder you wish in the \
  white entry box and press enter. \n \
  If you'd like to complete a reminder, press the white check button in the first column \
  of the table. \n \
  If you'd like to edit the contents of an existing reminder, click on the reminder \
  you'd wish to change and press enter when you're done. \n \
  If you'd like to edit the due date of a reminder, click on the box in the due date \
  column that corresponds with the row of the command you'd like to change\n\
  and press enter when you're dont. When editing \
  the due date, please use the month/date/year format. And if you'd like to remove said\
  due date, just delete all the contents of the box and press enter. If your reminder\
  isn't due today, the row is going to be cyan. But, if the reminder is due today or\
  or a day before today, the row with the reminder is marked purple.\n\
  I hope this information has been helpful to you and have a good day!"
  
  messagebox.showinfo(title = "Here's help", message = helpText) 
  
 def showList(self): #display list of reminders' contents
  self.masterframe = tk.Frame(self.root, borderwidth=2, relief = 'solid', width = 120)
  
  self.masterframe.columnconfigure(0, weight = 1)
  self.masterframe.columnconfigure(1, weight = 1)
  self.masterframe.columnconfigure(2, weight = 1)
  
  self.update()
  self.masterframe.pack(pady=10, padx=10)
  
 def update(self): #updates the table based on the back-end reminders list
  
  for widget in self.masterframe.winfo_children():
   widget.destroy()

  #adds all the labels first
  self.check = tk.Label(self.masterframe, background = "#ffb366", font = ('Arial', 12), text = "Check", borderwidth = 1, relief = 'solid', width = 5)
  self.check.grid(column = 0, row = 0)
  
  self.task = tk.Label(self.masterframe, background = "#ffb366", font = ('Arial', 12), text = "Task", borderwidth = 1, relief = 'solid', width = 50)
  self.task.grid(column = 1, row = 0)
  
  self.dueDate = tk.Label(self.masterframe, background = "#ffb366", font = ('Arial', 12), text = " Due Date", borderwidth = 1, relief = 'solid', width = 10)
  self.dueDate.grid(column = 2, row = 0)

  index = 1
  for x in self.listing: #inserts the contents of the reminders into the list
    color = self.orange
    if(x.dueDate == NULL or x.dueDate > datetime.now()):
     color = self.cyan

    destroy = partial(self.delete, index)
    date = tk.StringVar(self.masterframe)
    date.set(x.getDate())
    content = tk.StringVar(self.masterframe)
    content.set(x.cont)
    
    self.checkBox = tk.Button(self.masterframe, background = 'white', text = "x", font = ('Arial', 12), command = destroy, borderwidth = 1, relief = 'solid') 
    self.checkBox.grid(column = 0, row = index, sticky=tk.W + tk.E+tk.N +tk.S)
    
    self.remindList = tk.Entry(self.masterframe, background = color, font=('Arial', 12), textvariable = content, borderwidth = 1, justify = 'center',relief = 'solid') 
    self.remindList.grid(row = index, column = 1, sticky=tk.W + tk.E + tk.N + tk.S) 
    self.remindList.bind("<Return>", lambda event: self.edit(index, content.get()))
     
    self.remindDate = tk.Entry(self.masterframe, background = color, font = ('Arial', 12), textvariable = date, borderwidth = 1, relief = 'solid', justify = 'center', width = 10) 
    self.remindDate.grid(row = index, column = 2, sticky = tk.W + tk.E + tk.N + tk.S)
    self.remindDate.bind("<Return>", lambda event: self.date(index, date.get()))
    index += 1
    
 def date(self, index, input): #sets the date of the new reminder
  if(input == ""):
   analyse("remove-dueDate "+str(index-1), self.listing, self.memory)
  else:
   analyse("due "+str(index-1) +" "+input.strip(), self.listing, self.memory)
  self.update()

 def delete(self, input): #deletes the completed reminder
  analyse("complete "+str(input), self.listing, self.memory)
  self.update()

 def edit(self, index, input): #edits the content of an existing reminder
  analyse("edit "+ str(index-1) + " " +str(input), self.listing, self.memory)
  self.update()