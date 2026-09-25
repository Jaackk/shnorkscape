"""Phase-A composition using exact 950 native primitives; no new interface assets.

Static 9 owns every list visual and hit area. Static 11 owns its native scrollbar.
Static 4 owns a dynamic search field; the hidden ready marker 14 is untouched.
"""
def programs(api):
    push, ints, call, script = api
    host = lambda c: 1448 << 16 | c
    class Code:
        def __init__(self): self.ops=[]; self.labels={}; self.refs=[]
        def add(self, *ops): self.ops.extend(ops); return self
        def label(self, name): self.labels[name]=len(self.ops); return self
        def jump(self, op, name): self.refs.append((len(self.ops),op,name));self.ops.append((op,0));return self
        def raw(self, ni=0, ns=0, args=None):
            for at,op,name in self.refs:self.ops[at]=(op,self.labels[name]-at-1)
            raw=script(self.ops,ni,ns)
            if args is not None:
                import struct
                raw=bytearray(raw);raw[-9:-7]=struct.pack('>H',args);raw=bytes(raw)
            return raw
    il=lambda n:(0x35e,n)
    sl=lambda n:(0x25a,n)
    out={}
    # 21131: a bounded viewport; the scrollbar is built AFTER all rows.
    c=Code()
    for component,x,y,w,h in ((9,171,77,292,305),(11,465,77,16,305)):
        c.add(*ints(host(component)),(0x5,0),*ints(x,y,0,0,host(component)),(0x7c5,0),
              *ints(w,h,0,0,host(component)),(0x55,0),*ints(0,host(component)),(0xe4,0))
    for component in (10,12):c.add(*ints(1,host(component)),(0xe4,0))
    c.add(*ints(292),il(0),*ints(50),(0x51e,0),*ints(305),(0x1f9,0),*ints(host(9)),(0xa0,0),
          *ints(0,0,host(9)),(0x1a2,0))
    out[21131]=c.raw(1)
    # 21132: row border + one full-size native text actor in the SAME container.
    c=Code().add(*ints(host(9),3),il(0),*ints(2),(0x51e,0),(0x691,0),
                 *ints(290,47,0,0),(0x8c3,0),*ints(0),il(0),*ints(50),(0x51e,0),*ints(0,0),(0x828,0),
                 *ints(0x554d3b),(0x1a5,0))
    c.add(*ints(host(9)),il(0),*ints(2),(0x51e,0),*ints(1),(0x1d,0),*ints(8),
          il(0),*ints(50),(0x51e,0),*ints(2),(0x1d,0),*ints(0,0,276,43,0,0,2100),sl(0),call(2995))
    for n,label in ((1,'Select'),(2,'Spawn near me'),(3,'Place in world'),(4,'Inspect')):
        c.add(*ints(n),push(label),(0x83c,0))
    c.add(*ints(21134,-2147483644),il(0),il(1),sl(1),push('iiis'),(0x6a,0))
    out[21132]=c.raw(2,2)
    out[21133]=script(ints(host(11),host(9))+[call(7791)])
    # 21134: immediate local highlight, followed by server-authoritative nonce.
    c=Code().add(*ints(0),(0x592,3)).label('loop').add(il(3),il(2)).jump(0x647,'done')
    c.add(*ints(host(9)),il(3),*ints(2),(0x51e,0),(0x109,0),*ints(1)).jump(0x412,'next')
    c.add(*ints(0x554d3b),(0x1a5,0),il(3),il(1)).jump(0x412,'next')
    c.add(*ints(0xffd479),(0x1a5,0)).label('next').add(il(3),*ints(1),(0x1d,0),(0x592,3)).jump(0x713,'loop')
    c.label('done').add(il(0),sl(0),call(21127))
    out[21134]=c.raw(4,1,args=3)
    # 21135: create/update a full NPC model; every selection resets old sequence.
    # Native CS3503 recreates the model actor when its subject changes. Reuse
    # its slot, not a stale rendered model; the list/search remain untouched.
    c=Code().add(*ints(host(7),6),il(0),(0x691,0),*ints(224,130,0,0),(0x8c3,0),*ints(501,93,0,0),(0x828,0))
    c.add(il(1),(0x1fc,0),*ints(-1),(0x67f,0),il(2),(0x67f,0),
                         *ints(0),il(4),*ints(0,180,0),il(3),(0x112,0))
    out[21135]=c.raw(5)
    # 21136: drag-to-rotate uses native CS11619's hooks with a dynamic model slot.
    # Static 6 is a drag layer under actor host5; static 4 is reserved for search.
    c=Code()
    for child in (20,21,22):c.add(*ints(1,host(child)),(0xe4,0))
    c.add(*ints(501,93,0,0,host(6)),(0x7c5,0),*ints(224,130,0,0,host(6)),(0x55,0),*ints(0,host(6)),(0xe4,0),
          *ints(host(6)),(0x8a2,0),*ints(-1,host(6)),(0x353,0))
    for sid,op in ((8479,0x815),(8480,0x732)):
        c.add(*ints(sid,host(6),host(7)),il(0),push('iii'),*ints(host(6)),(op,0))
    c.add(*ints(189,host(6)),(0x413,0))
    out[21136]=c.raw(1)
    # 21137: create integrated search. No native text component is repurposed.
    c=Code().add(*ints(8,38,0,0,host(4)),(0x7c5,0),
                 *ints(320,30,0,0,host(4)),(0x55,0),*ints(0,host(4)),(0xe4,0))
    for child in (17,18,19):c.add(*ints(1,host(child)),(0xe4,0))
    c.add(*ints(host(4),0),(0x109,0),*ints(1)).jump(0x647,'exists')
    c.add(*ints(host(4),0,8,3,0,0,304,24,0,0,2100),sl(0),call(2995))
    c.add(*ints(host(4),3,1),(0x691,0),*ints(320,30,0,0),(0x8c3,0),*ints(0,0,0,0),(0x828,0),*ints(0xa99b78),(0x1a5,0))
    c.label('exists').add(*ints(host(4),0),(0x109,0),*ints(1)).jump(0x412,'done')
    c.add(*ints(1),push('Search'),(0x83c,0),*ints(21138),sl(1),sl(0),push('ss'),(0x6a,0),sl(0),(0x1f1,0))
    c.add(sl(0),push(''),(0x3f,0),*ints(0)).jump(0x412,'done')
    c.add(sl(2),(0x1f1,0)).label('done')
    out[21137]=c.raw(0,3)
    # 21138: acquire EXACT native text context11, bind the keycode/character
    # sentinels from CS9833, reuse its editor buffer while the field owns focus.
    c=Code().add(sl(1),(0x195,34130432),sl(1),(0x25f,0),(0x195,33817856),
                 *ints(11,1),call(8841),*ints(21139,-2147483640,-2147483639),sl(0),sl(1),push('iiss'),*ints(host(4)),(0x375,0),
                 *ints(host(4),0),(0x109,0),*ints(1)).jump(0x412,'done')
    c.add(sl(1),push('|'),(0x267,2),(0x1f1,0)).label('done')
    out[21138]=c.raw(0,2)
    # 21139: edit locally, submit only on Enter; Escape cancels. CS13121 proves
    # key84=submit /13=cancel. No chat packet and no per-keystroke server redraw.
    c=Code().add((0x1ca,34289920),*ints(11)).jump(0x412,'done')
    c.add(il(0),*ints(84)).jump(0x647,'submit').add(il(0),*ints(13)).jump(0x647,'cancel')
    for key in (96,97,98,99,102,103):c.add(il(0),*ints(key)).jump(0x647,'cursor')
    c.add((0x1ca,33817856),(0x1ca,34130432),*ints(8),il(0),il(1),*ints(80),call(7170),
          (0x195,33817856),(0x195,34130432),*ints(host(4),0),(0x109,0),*ints(1)).jump(0x412,'done')
    c.add((0x1ca,34130432),push('|'),(0x267,2),(0x1f1,0)).jump(0x713,'done')
    c.label('submit').add(sl(0),(0x1ca,34130432),(0x267,2),(0x77b,0),call(21140)).jump(0x713,'done')
    c.label('cancel').add(sl(1),(0x195,34130432),call(21140)).jump(0x713,'done')
    c.label('cursor').add(il(0),(0x1ca,33817856),(0x1ca,34130432),call(1553),(0x195,33817856)).label('done')
    out[21139]=c.raw(2,2)
    c=Code().add((0x1ca,34289920),*ints(11)).jump(0x412,'done')
    c.add(*ints(-1),push(''),*ints(host(4)),(0x375,0),*ints(11,0),call(8841),
                 *ints(host(4),0),(0x109,0),*ints(1)).jump(0x412,'done')
    c.add((0x1ca,34130432),(0x1f1,0)).label('done')
    out[21140]=c.raw()
    c=Code().add(*ints(host(7)),il(0),(0x109,0),*ints(1)).jump(0x412,'done')
    c.add(*ints(-1),(0x67f,0),il(1),(0x67f,0)).label('done')
    out[21141]=c.raw(2)
    c=Code().add(*ints(host(7)),il(0),(0x109,0),*ints(1)).jump(0x412,'done')
    c.add(sl(0),(0x1f1,0)).label('done')
    out[21142]=c.raw(1,1)
    # 21143: replace only presentation buttons' callbacks. A sequence validated
    # for the selected NPC plays locally; absent bindings have no menu/hook.
    c=Code().add(*ints(host(5)),il(0),(0x109,0),*ints(1)).jump(0x412,'done')
    c.add(il(2),*ints(-1)).jump(0x647,'disabled')
    c.add(*ints(1),push('Preview'),(0x83c,0),*ints(21141),il(1),il(2),push('ii'),(0x6a,0)).jump(0x713,'done')
    c.label('disabled').add(*ints(1),push(''),(0x83c,0),*ints(-1),push(''),(0x6a,0)).label('done')
    out[21143]=c.raw(3)
    # 21144: actual item rows, with native inventory sprites. Active SETOBJ550
    # and explicit3c0 share the exact950 item setter (1401d1020); only the
    # component lookup wrapper differs. No rendered-image substitutes.
    c=Code().add(*ints(host(9),3),il(0),*ints(3),(0x51e,0),(0x691,0),
                 *ints(290,47,0,0),(0x8c3,0),*ints(0),il(0),*ints(50),(0x51e,0),*ints(0,0),(0x828,0),
                 *ints(0x554d3b),(0x1a5,0))
    c.add(*ints(host(9),5),il(0),*ints(3),(0x51e,0),*ints(1),(0x1d,0),(0x691,0),
          *ints(40,40,0,0),(0x8c3,0),*ints(4),il(0),*ints(50),(0x51e,0),*ints(3),(0x1d,0),*ints(0,0),(0x828,0),
          il(1),*ints(1),(0x550,0))
    c.add(*ints(host(9)),il(0),*ints(3),(0x51e,0),*ints(2),(0x1d,0),*ints(48),
          il(0),*ints(50),(0x51e,0),*ints(2),(0x1d,0),*ints(0,0,236,43,0,0,2100),sl(0),call(2995),
          *ints(1),push('Select'),(0x83c,0),*ints(21127,-2147483644),sl(1),push('is'),(0x6a,0))
    out[21144]=c.raw(2,2)
    out[21145]=script(ints(host(3))+[(0x5,0)])
    # Inspector item icon: slot, item, x, y, width, height.
    c=Code().add(*ints(host(7),5),il(0),(0x691,0),il(4),il(5),*ints(0,0),(0x8c3,0),
                 il(2),il(3),*ints(0,0),(0x828,0),il(1),*ints(1),(0x550,0))
    out[21146]=c.raw(6)
    # Model zoom changes retain the native drag angles (CS1165 getter order).
    c=Code().add(*ints(host(7)),il(0),(0x109,0),*ints(1)).jump(0x412,'done')
    c.add((0x763,0),(0x25d,0),(0x836,0),(0x804,0),(0x73b,0),il(1),*ints(0)).jump(0x647,'reset')
    c.add((0x526,0),il(1),(0x1d,0),*ints(50),(0x1f9,0),*ints(6000),(0x4d3,0)).jump(0x713,'apply')
    c.label('reset').add(il(2)).label('apply').add((0x112,0)).label('done')
    out[21147]=c.raw(3)
    c=Code().add(*ints(host(5)),il(0),(0x109,0),*ints(1)).jump(0x412,'done')
    c.add(*ints(21147),il(1),il(2),il(3),push('iii'),(0x6a,0)).label('done')
    out[21148]=c.raw(4)
    # Local-player model setter confirmed by native CS3503.
    c=Code().add(*ints(host(7),6),il(0),(0x691,0),*ints(270,275,0,0),(0x8c3,0),*ints(180,85,0,0),(0x828,0),
                 (0x79b,0),*ints(0,0,0,180,0,900),(0x112,0))
    out[21149]=c.raw(1)
    # Generic collection row: select only; it cannot inherit NPC spawn operations.
    c=Code().add(*ints(host(9),3),il(0),*ints(2),(0x51e,0),(0x691,0),
                 *ints(290,47,0,0),(0x8c3,0),*ints(0),il(0),*ints(50),(0x51e,0),*ints(0,0),(0x828,0),*ints(0x554d3b),(0x1a5,0))
    c.add(*ints(host(9)),il(0),*ints(2),(0x51e,0),*ints(1),(0x1d,0),*ints(8),il(0),*ints(50),(0x51e,0),*ints(2),(0x1d,0),
          *ints(0,0,276,43,0,0,2100),sl(0),call(2995),*ints(1),push('Select'),(0x83c,0),*ints(21127,-2147483644),sl(1),push('is'),(0x6a,0))
    out[21150]=c.raw(1,2)
    # Save native scroll before rebuilding a collection's children. Search,
    # domain and page changes request a reset; inspector actions do not.
    c=Code().add(*ints(host(9)),(0xd1,0),(0x592,2),il(0),call(21131),il(1),*ints(0)).jump(0x412,'done')
    c.add(*ints(0),il(2),*ints(host(9)),(0x1a2,0)).label('done')
    out[21151]=c.raw(3,args=2)
    # Search button reads the native editor buffer if focused; otherwise reruns
    # the existing query. It replaces only this button's native operation.
    c=Code().add(*ints(host(5)),il(0),(0x109,0),*ints(1)).jump(0x412,'done')
    c.add(*ints(21153),sl(0),sl(1),push('ss'),(0x6a,0)).label('done')
    out[21152]=c.raw(1,2)
    c=Code().add(sl(0),(0x1ca,34289920),*ints(11)).jump(0x412,'unfocused')
    c.add((0x1ca,34130432)).jump(0x713,'submit')
    c.label('unfocused').add(sl(1)).label('submit').add((0x267,2),(0x77b,0),call(21140))
    out[21153]=c.raw(0,2)
    # Consistent selected border for item and generic collection rows.
    c=Code().add(*ints(host(9)),il(0),il(1),(0x51e,0),(0x109,0),*ints(1)).jump(0x412,'done')
    c.add(*ints(0xffd479),(0x1a5,0)).label('done')
    out[21154]=c.raw(2)
    # Parameterised local-player preview and matching native rotation hit region.
    c=Code().add(*ints(host(7),6),il(0),(0x691,0),il(3),il(4),*ints(0,0),(0x8c3,0),il(1),il(2),*ints(0,0),(0x828,0),
                 (0x79b,0),*ints(0,100,0,0,0),il(5),(0x112,0))
    out[21155]=c.raw(6)
    c=Code().add(il(1),il(2),*ints(0,0,host(6)),(0x7c5,0),il(3),il(4),*ints(0,0,host(6)),(0x55,0),
                 *ints(0,host(6)),(0xe4,0),*ints(host(6)),(0x8a2,0),*ints(-1,host(6)),(0x353,0))
    for sid,op in ((8479,0x815),(8480,0x732)):
        c.add(*ints(sid,host(6),host(7)),il(0),push('iii'),*ints(host(6)),(op,0))
    c.add(*ints(189,host(6)),(0x413,0))
    out[21156]=c.raw(5)
    return out
