// mapreduce functions

// start by combining the hipparcos and sao data. I'm making a call that the hipparcos data is more accurate than
// the sao
function getPreferredValue() {
    var length = arguments.length;
    for (var i = 0; i < length; i++) {
        if (arguments[i] != null) {
            return arguments[i];
        }
    }
    return null;
}

db.system.js.save({_id:"getPreferredValue", value: function() {
    var length = arguments.length;
    for (var i = 0; i < length; i++) {
        if (arguments[i] != null) {
            return arguments[i];
        }
    }
    return null;
}});

db.runCommand({
    mapreduce:"catalog",
    query:{xref:/^[HIP,SAO]/i},
    sort:{name:1},
    limit:500000,
    map:function() {
        emit({dummy:this.xref}, this); // give me the whole damn object then
    },
    reduce:function(key, values) {
        var tempObject = {hipObject: {bii: null, lii: null, class: null, radeg: null, decdeg: null, rapm:null, decpm:null, vmag:null, parallax:null, xref:null}
            , saoObject:{bii: null, lii: null, class: null, radeg: null, decdeg: null, rapm:null, decpm:null, vmag:null, parallax:null, xref:null}};
        try {
            for (var i = 0; i < values.length; i++) {
                if (values[i].name.indexOf("HIP") != -1) {
                    var hipObject = {bii: null, lii: null, class: null, radeg: null, decdeg: null, rapm:null, decpm:null, vmag:null, parallax:null, xref:null};
                    hipObject.bii = values[i].bii;
                    hipObject.lii = values[i].lii;
                    hipObject.class = values[i].class;
                    hipObject.radeg = values[i].radeg;
                    hipObject.decdec = values[i].decdeg;
                    hipObject.rapm = values[i].rapm;
                    hipObject.decpm = values[i].decpm;
                    hipObject.vmag = values[i].vmag;
                    hipObject.parallax = values[i].parallax;
                    hipObject.xref = values[i].xref;
                    tempObject.hipObject = hipObject;
                }
                if (values[i].name.indexOf("SAO") != -1) {
                    var saoObject = {bii: null, lii: null, class: null, radeg: null, decdeg: null, rapm:null, decpm:null, vmag:null, parallax:null, xref:null};
                    saoObject.bii = values[i].bii;
                    saoObject.lii = values[i].lii;
                    saoObject.class = values[i].class;
                    saoObject.radeg = values[i].radeg;
                    saoObject.decdec = values[i].decdeg;
                    saoObject.rapm = values[i].rapm;
                    saoObject.decpm = values[i].decpm;
                    saoObject.vmag = values[i].vmag;
                    saoObject.parallax = values[i].parallax;
                    saoObject.xref = values[i].xref;
                    tempObject.saoObject = saoObject;
                }
            }

            var result = {bii: null, lii: null, class: null, radeg: null, decdeg: null, rapm:null, decpm:null, vmag:null, parallax:null, xref:null}
            result.bii = getPreferredValue(tempObject.hipObject.bii, tempObject.saoObject.bii);
            result.lii = getPreferredValue(tempObject.hipObject.lii, tempObject.saoObject.lii);
            result.class = getPreferredValue(tempObject.hipObject.class, tempObject.saoObject.class);
            result.radeg = getPreferredValue(tempObject.hipObject.radeg, tempObject.saoObject.radeg);
            result.decdeg = getPreferredValue(tempObject.hipObject.decdeg, tempObject.saoObject.decdeg);
            result.rapm = getPreferredValue(tempObject.hipObject.rapm, tempObject.saoObject.rapm);
            result.decpm = getPreferredValue(tempObject.hipObject.decpm, tempObject.saoObject.decpm);
            result.vmag = getPreferredValue(tempObject.hipObject.vmag, tempObject.saoObject.vmag);
            result.parallax = getPreferredValue(tempObject.hipObject.parallax, tempObject.saoObject.parallax);
            result.xref = tempObject.hipObject.xref;
        } catch(e) {
        }

        return result;
    },
    out: {reduce:"catalog2", db:"astro3"}
});
