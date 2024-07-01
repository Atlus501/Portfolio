#my remind object and its methods 
from asyncio.windows_events import NULL
from datetime import datetime

class Remind:

 cont = ""
 dueDate = NULL

 def __init__(self, cont): #constructor for reminder object
  self.cont = cont

 def setCont(self, cont): #edits the content of reminder
  self.cont = cont

 def setDate(self, year, month, date): #sets the date of the reminder
  if(year == 0 and month == 0 and date == 0):
   self.dueDate = NULL
  else:
   self.dueDate = datetime(year, month, date)
   
 def getDate(self): #returns the date of the Remind object
  if(self.dueDate == NULL):
   return ""
  else:  
   return str(self.dueDate.month) + "/" + str(self.dueDate.day) + "/" +str(self.dueDate.year) 

 def show(self, idd):#shows the content of the reminder
  print("Task " + idd + ") " + self.cont)
  if(self.dueDate != NULL):
   if(self.dueDate == datetime.now()):
    print(" This task is due today:")
   elif(self.dueDate < datetime.now()):
    print(" This task is past due:")
   
   x = self.dueDate
   print(" Due: " + x.strftime("%x"))