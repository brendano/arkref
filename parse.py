#!/usr/bin/env python
import sys,os
from subprocess import *
file = sys.argv[1]
parse_out = open(file.replace(".sent","") + ".parse",'w')
ner_out = open(file.replace(".sent","") + ".ner",'w')
file = file.replace(".sent","") + ".sent"
for sent in open(file):
  sent = sent.strip()

  p = Popen("nc localhost 5556", shell=True, stdin=PIPE, stdout=PIPE)
  print>>p.stdin, sent
  p.stdin.close()
  parse = p.stdout.read()
  parse = parse.split("\n")[0]
  print>>parse_out, parse

  p = Popen("nc localhost 1234", shell=True, stdin=PIPE, stdout=PIPE)
  print>>p.stdin, sent
  p.stdin.close()
  ner = p.stdout.read()
  ner = ner.strip()
  print>>ner_out, ner


parse_out.close()
ner_out.close()

