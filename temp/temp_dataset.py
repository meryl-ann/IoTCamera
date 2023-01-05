import csv
import random

with open("./IOT-temp.csv", 'r') as file:
    csvreader = csv.reader(file)
    temp = random.choice([row[0] for row in csvreader])
    temp_int = int(temp)
    print(temp_int)
    #if temp_int > 36:
    #    print("you have fever, pls go to doctor")
    #else:
    #    print("pls proceed to face mask detector")
