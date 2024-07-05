from helperFunctions import *
from remind import Remind
from mergesort import *
import copy

# all of the command prompt methods
def analyse(input, list0, Backups): #the command prompt function

 words = input.split(" ", 1)
 
#list of commands
  
 if(words[0] != "undo"): #saves the previous list if the user doesn't use undo command
  index = 0
  listcopy = copy.deepcopy(list0)
  Backups.append(listcopy)
  
 try:
     if(len(words) == 0): #catch cases where the user accidently presses enter
      print("please ender a command")
  
     elif(words[0] == "undo"): #undoes the previous action
      if len(Backups) == 0:
       print("Cannot undo further")
      else:
       list0[:] = (Backups[len(Backups) -1])[:]
       del Backups[len(Backups) -1]
       print("Changes undone successfully")

     elif(words[0] == "add"): #adds a new reminder onto the list
      if(len(words) < 2):
       print("Please state what you'd like to add")
      else:
       newReminder = Remind(words[1].strip())
       list0.append(newReminder)
  
     elif(words[0] == "end"): #ending program
      print("Have a good day!")
      return True
 
     elif(words[0] == "clear"): #clears the entire list
      list0.clear()
      print("Reminders list successfully cleared.")

     elif(words[0] == "complete"): #completes the reminder
      if(len(words) < 2):
       print("Please specify which reminder you'd like to be completed.")
   
      else: #removes the reminder at that index
       result = checkIndex(words[1], list0)
       if(result != -1):
        del list0[result]
       else:
        print("Your reminder wasn't found.")
        print("Please enter the full reminder or an index from 1 to " + str(len(list0)) + ".")

     elif(words[0] == "edit"): #edits the reminder at that index
      sep = (words[1].strip()).split(" ", 1)   

      index = checkIndex(sep[0], list0)
      if(index != -1):
        list0[index].setCont(sep[1])

     elif(words[0] == "sort"): #this is the method that sorts the reminders
      list0[:] = mergeSort(list0)
 
     elif(words[0] == "due"): #edits the due date of the reminder
       sep = words[1].rsplit(" ", 1)
       try: 
        date = sep[1].split("/", 2)
       except IndexError:
        print("Please print the date in this format: 'due' task-number month/date/year")
        print("You've entered:")
        print(sep[1])
   
       if(len(date) != 3 or (not date[0].isdigit()) or (not date[1].isdigit()) or (not date[2].isdigit())):
        print("Please enter the date in this format: month/date/year.")
       else:
        result = checkIndex(sep[0], list0)
        if(result != -1):
          list0[result].setDate(int(date[2]), int(date[0]), int(date[1]))
  
     elif(words[0] == "remove-dueDate"):
       index = checkIndex(words[1].strip(), list0)
       list0[index].setDate(0,0,0)
 
     else: #default catch all for unknown commands
      print("Invalid command, please try another or type 'help' to see command list")
      
 except IndexError:
   print("Please enter a valid command.")
   print("If you need a guide on the commands and their valid formats,")
   print("type 'help' in the command input")
   print("You've entered:")
   print(input)
  
 return False