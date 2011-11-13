import os, sys, wikipedia, login, category, fnmatch, time, pagegenerators, family, code

global delimeter

def getAccounts(pageContent):
    accounts = []
    lines = pageContent.split("\n")
    for line in lines:
        if line.startswith("* "):
            line = line[2:len(line)]
            coincidence = line.find(" in ")
            #print ("coincidence",coincidence)
            if coincidence == -1:
                if line.find("http://") != -1 or line.find("www") != -1:
                    accounts.append(lineWithoutEndSpaces(line))
                else:
                    print("Ignore line:"+line)
            else:
                user = lineWithoutEndSpaces(line[0:coincidence])
                #print "user:"+user
                userName = lineWithoutBeginSpaces(user)
                #print "userName:"+userName
                account = lineWithoutEndSpaces(line[coincidence+4:len(line)])
                #print "account:"+account
                isIn = False
                for community in communities:
                    if account.find(community) != -1:
                        accountName = lineWithoutBeginSpaces(account)
                        #print "accountName:"+accountName
                        accounts.append(userName+delimeter+accountName)
                        isIn = True
                        break
                if not isIn:
                    print("Ignore line without community:"+line)
    return accounts

def lineWithoutBeginSpaces(line):
    while 1 == 1:
        if len(line) == 0 or line[0] != ' ':
            break
        line = line[1:len(line)]
    return line

def lineWithoutEndSpaces(line):
    while 1 == 1:
        if len(line) == 0 or line[len(line)-1] != ' ':
            break
        line = line[0:len(line)-1]
    return line

delimeter = "^#^"
accountsDelimiter = ","
userDelimiter = "\n"
usersPredefined = {"owasp":"9","mitre":"10"}
communities = ["serverfault.com","questions.securitytube.net","security.stackexchange.com",
               "stackoverflow.com","webapps.stackexchange.com","ohloh.net",
               "sla.ckers.org","elhacker.net"]
fileName = "users-"+str(int(time.time()))

# login to the wiki
ex = wikipedia.Site('en','mediawiki','administrador')
loginManager = login.LoginManager('vulneranet', True, ex)
loginManager.login()
login.show(ex, True)

pathu = '/index.php?title=Special:ListUsers&useskin=monobook'
pageContent = ex.getUrl(pathu, sysop = True)
htmlEncoded = pageContent.encode('ascii','ignore')
#print htmlEncoded

users = {}

while 1 == 1:
    coincidence1 = htmlEncoded.find("<a href=\"/semanticwiki/index.php/User:")
    coincidence2 = htmlEncoded.find("<a href=\"/semanticwiki/index.php?title=User:")
    if(coincidence1 < coincidence2 or coincidence2 == -1):
        coincidence = coincidence1
    else:
        coincidence = coincidence2
    #print("coincidence:"+str(coincidence))
    if (coincidence == -1):
        break
    htmlEncoded = htmlEncoded[coincidence:len(htmlEncoded)]
    endCoincidence = htmlEncoded.find("</a>")
    #print endCoincidence
    if(endCoincidence == -1):
        break    
    name = htmlEncoded[0:endCoincidence]
    htmlEncoded = htmlEncoded[endCoincidence:len(htmlEncoded)]
    endCoincidence = name.rfind(">")
    name = name[endCoincidence+1:len(name)]
    users[name] = name
    #print("Name extracted:"+name)

fileHandle = open(fileName,"w")
#print "Users is added in file: "+fileName
for userName in users.keys():
    isIn = False
    for userPredefined,userReputation in usersPredefined.items():
        if userName.lower() == userPredefined.lower():
            fileHandle.write(userName+":"+userReputation+userDelimiter)
            isIn = True
            break
    if isIn:
        continue
    page = wikipedia.Page(ex,"User:"+userName)
    if page.exists():
        pageContent = page.get()
        #print userName
        accounts = getAccounts(pageContent)
        accountsToLine = ""
        for account in accounts:
            if len(accountsToLine) > 0:
                accountsToLine = accountsToLine + accountsDelimiter
            accountsToLine = accountsToLine + account        
        fileHandle.write(userName+":"+accountsToLine+userDelimiter)
    else:
        fileHandle.write(userName+":not_exist"+userDelimiter)
        #print userName+":not_exist"

fileHandle.close()


