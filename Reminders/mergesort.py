from asyncio.windows_events import NULL
from remind import Remind

def sliceLeft(list1): #this splits the list 
  result = list1[0 : len(list1): 2]
  return result

def sliceRight(list1): #this is another function that splits the list
  result = list1[1 : len(list1): 2]
  return result

def merge(list1, list2): #this is the function that merges the lists together

 result = []
 
 while len(list1) + len(list2) != 0 :
    if(len(list1) == 0): #adds list2 if list1 is empty
     result.append(list2[0])
     sample2 = list2[1 : len(list2)]
     list2 = sample2
    elif(len(list2) == 0): #adds list1 if list2 is empty
     result.append(list1[0])
     list1 = list1[1 : len(list1)]
    elif((list1[0].dueDate != NULL or list2[0].dueDate != NULL) or (list1[0].dueDate != list2[0].dueDate)):
     if(list2[0].dueDate == NULL):
       result.append(list1[0])
       list1 = list(list1[1:len(list1)])
     elif(list1[0].dueDate == NULL):
       result.append(list2[0])
       list2 = list(list2[1:len(list2)])
     elif(list1[0].dueDate < list2[0].dueDate): #sorts it by due year first
      result.append(list1[0])
      list1 = list(list1[1: len(list1)])
     else:
      result.append(list2[0])
      list2 = list(list2[1: len(list2)])
    else:
     if(list1[0].cont < list2[0].cont): #sorts it alphebetically last
       result.append(list1[0])
       list1 = list(list1[1: len(list1)])
     else:
       result.append(list2[0])
       list2 = list(list2[1: len(list2)])
      
 return result
 

def mergeSort(list1): #the main mergesort method
  
  if(len(list1) > 1):
   left = sliceLeft(list1)
   right = sliceRight(list1)
   test1 = mergeSort(left)
   test2 = mergeSort(right)
   return merge(test1, test2)
  else:
    return list1
