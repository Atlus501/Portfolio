#this is my reminders program code file
from commandprompt import *
from MyGUI import MyGUI

#two starting variables 
Reminders = []
Memory = []
finish = False

'''
while finish == False:
 
 printList(Reminders)
 
 command = input("Enter your command: ")
 
 command = command.strip()

 finish = analyse(command, Reminders, Memory)
 
save(Reminders)
'''


load(Reminders)
MyGUI(Reminders, Memory)
save(Reminders)