from __future__ import unicode_literals
#from java import *
import os
import youtubedl as youtube_dl

def download_youtube (url):
	print('test')
	print(os.environ["HOME"])
	os.chdir(os.environ["HOME"])
	os.chdir('/sdcard/Download')
	
	ydl_opts = {
		#'cachedir': False,
		#'simulate': True,
		#'outtmpl': 'Internal shared storage/Download/%(title)s.%(ext)s'
	}
	with youtube_dl.YoutubeDL(ydl_opts) as ydl:
		ydl.download([str(url)])
	print ('Hello')
	print ('Hello')
	print ('Hello')
	print ('Hello')