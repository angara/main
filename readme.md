
# Angara.Net: main page app

'''
https://mothereff.in/js-escapes
'''


'''
A code point C greater than 0xFFFF corresponds to a surrogate pair <H, L> as per the following formula:

H = Math.floor((C - 0x10000) / 0x400) + 0xD800
L = (C - 0x10000) % 0x400 + 0xDC00

The reverse mapping, i.e. from a surrogate pair <H, L> to a Unicode code point C, is given by:

C = (H - 0xD800) * 0x400 + L - 0xDC00 + 0x10000

'''
