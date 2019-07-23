def run_custom_arguments (argument):
    import os, sys
    os.chdir('/storage/emulated/0/Download')

    sys.stdout = open('logger.txt', 'w')
    sys.stderr = open('logger.txt', 'w')

    try:
        splitted = argument.split()
        
        for i in range (0, len(sys.argv)-1):
            del sys.argv[1]
        
        for items in splitted: 
            sys.argv.append(items)
            print(sys.argv)
    except: 
        print("error importing arguments")

    #sys.argv.append('https://www.youtube.com/watch?v=Ie4YyBbkUrw')
    #sys.argv.append('-v')


    import youtubedl as youtube_dl 
    try:
        youtube_dl.main()
        for i in range (0, len(sys.argv)-1):
            del sys.argv[1]
    except: 
        print("error launching youtube-dl with arguments")
        for i in range (0, len(sys.argv)-1):
            del sys.argv[1]