#extra helper methods
from asyncio.windows_events import NULL
from remind import Remind

def checkIndex(searched, list): #it checks if the index is valid or not

 searched = searched.strip()

 if(searched.isdigit()):
  searched = int(searched) -1
 else:
  return -1

 if(searched >= len(list) or searched <0):
    print("Your reminder wasn't found")
    print("You've entered index:")
    print(searched)
    
    return -1
 else:
  print("Index: "+str(searched))
  return searched

def load(list): #loads in all the data from a document
 saver = open("Safe.txt", "a+")
 saver.seek(0)
 read = saver.readlines()
 index = 0
 try:
  while len(list) != len(read):
   sep = read[index].rsplit(" ", 1)
   date = sep[1].split("/", 2)
   list.append(Remind(sep[0].strip()))
   if(int(date[0]) != 0 and int(date[1]) != 0 and int(date[2] != 0)):
    list[index].setDate(int(date[2]), int(date[0]), int(date[1]))
   index += 1
 except IndexError:
  saver.close()
  list.clear()
  
 saver.close()

def save(list): #saves all the data from the Reminders in the note
  saver = open("Safe.txt", "w")
  index = 0
  while index < len(list):
    #saver.write((list[index].cont + " " + str(list[index].dueMonth)+"/"+str(list[index].dueDate)+"/"+str(list[index].dueYear)).strip()+"\n")
    if(list[index].dueDate == NULL):
      saver.write(list[index].cont + " 0/0/0\n")
    else:
     savedDate = list[index].dueDate
     saver.write(list[index].cont + " " + str(savedDate.month) + "/" +str(savedDate.day) + "/" +str(savedDate.year)+"\n")
    index += 1
    
  saver.close()

def printList(list): #the show list function
 print("---------------")
  
 if(len(list) == 0):
   print("You don't have any reminders right now")
 else:
   print("Here are your reminders:")
   number = 1
   for x in list:
    idd = str(number)
    x.show(idd)
    number += 1
    
 print("---------------")