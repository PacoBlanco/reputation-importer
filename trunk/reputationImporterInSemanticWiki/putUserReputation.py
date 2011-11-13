import os, sys, wikipedia, login, category, fnmatch, delete

# login to the wiki
#ex = wikipedia.Site('en')
ex = wikipedia.Site('en','mediawiki','administrador')
loginManager = login.LoginManager('vulneranet', False, ex)
loginManager.login()
#print 'logged in'
login.show(ex, False)

reputationName = "User Reputation"
directory = './'
lastNumber = 0

for fileName in os.listdir ( directory ):
    if not fileName.startswith('reputation-'):
        #print(fileName+" is discarted")
        continue
    try:
        number = int(fileName[11:len(fileName)])
    except ValueError:
        print "Error in FileName Format: "+fileName
        continue
    if(number > lastNumber):
        lastNumber = number
        lastFile = fileName

if lastNumber == 0:
    print "There is not user reputation files"
    sys.exit()

print "select file: "+lastFile

fileHandle = open(lastFile)
lines = ""
for line in fileHandle:
    lines += line.decode('ascii','replace')
fileHandle.close()

#page = wikipedia.Page(ex,reputationName)
#exist = page.exists()
#if exist:
#    print "Erase "+reputationName
#    #os.system('python delete.py -page:User_Reputation -always -summary:"(Script) user reputation table is updated"')
#    page.delete('(Script) user reputation table is updated)')

page = wikipedia.Page(ex,reputationName)
print "Put\n"+lines
page.put(lines,'(Script) User Reputation updated')
