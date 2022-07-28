#!/usr/bin/python3

import pygame
import os

pygame.init()

src_name = input("Source image: ")

split = os.path.splitext(src_name)

def_dst_name = split[0]+"_tint"+split[1]

dst_name = input(f"Destination image (blank for default '{def_dst_name}'): ")

if dst_name == "":
    dst_name = def_dst_name


src = pygame.image.load(src_name)
dst = pygame.Surface((src.get_width(), src.get_height()), pygame.SRCALPHA)

def mix(x, y, a):
    return x*(1-a) + y*a

DAMAGE_TINT = (255, 0, 0)
DAMAGE_TINT_FACTOR = 178/255

def norm(v) -> int:
    return int(max(0, min(round(v), 255)))

def mix_rgb(rgb1, rgb2, a):
    return (
        norm(mix(rgb1[0], rgb2[0], a)),
        norm(mix(rgb1[1], rgb2[1], a)),
        norm(mix(rgb1[2], rgb2[2], a))
        )

for x in range(src.get_width()):
    for y in range(src.get_height()):
        in_col = src.get_at((x, y))
        in_rgb = in_col[:3]
        in_extra = in_col[3:]

        out_col = mix_rgb(DAMAGE_TINT, in_rgb, DAMAGE_TINT_FACTOR) + in_extra
        dst.set_at((x, y), out_col)

pygame.image.save(dst, dst_name)
