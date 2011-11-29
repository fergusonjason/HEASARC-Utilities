// mapreduce functions

// start by combining the hipparcos and sao data. I'm making a call that the hipparcos data is more accurate than
// the sao
function map() {
    var resultObject;

    resultObject.xref = this.xref;

    // is this a hip or a SAO?
    if (this.name.search("/^HIP/i") != -1) {
        resultObject.bii.hip = this.bii;
        resultObject.lii.hip = this.lii;
        resultObject.radeg.hip = this.radeg;
        resultObject.decdeg.hip = this.decdeg;
        resultObject.rapm.hip = this.rapm;
        resultObject.decpm.hip = this.decpm;
        resultObject.class.hip = this.class;
        resultObject.parallax.hip = this.parallax;
        resultObject.vmag.hip = this.vmag;
    } else {
        resultObject.bii.hip = this.bii;
        resultObject.lii.sao = this.lii;
        resultObject.radeg.sao = this.radeg;
        resultObject.decdeg.sao = this.decdeg;
        resultObject.rapm.sao = this.rapm;
        resultObject.decpm.sao = this.decpm;
        resultObject.class.sao = this.class;
        resultObject.parallax.sao = this.parallax;
        resultObject.vmag.sao = this.vmag;
    }
    print(resultObject.toSource());
    emit({dummy:this.xref}, resultObject);
}

function reduce(key, values) {
    var result = {test:"test"};

    print(key);
    //print(values.toSource());

    return result;
}

db.runCommand(
{
    mapreduce:"catalog",
    map: map,
    reduce: reduce,
    out:{replace:"catalog2",db:"astro3"},
    query: {$or:[
        {name:/^HIP/i},
        {name:/^SAO/i}
    ]}
}
        );

db.catalog.mapReduce(map, reduce, {out: {replace:"catalog2",db:"astro3"}});

db.catalog.find({$or:[
    {name:/^HIP/i},
    {name:/^SAO/i}
]});

// set the sao values to the hip values
db.catalog.find({$and:[{xref:/^HIP/i},{xref:/^SAO/i}]}).forEach(function(obj) {
    var xrefArray = obj.xref;
    var hipnum;
    var saonum;
    var myObjId = 0;

    for (var i = 0; i < xrefArray.length; i++) {
        if (xrefArray[i].indexOf("HIP") != -1) {
            hipnum = xrefArray[i];
        }
        if (xrefArray[i].indexOf("SAO") != -1) {
            saonum = xrefArray[i];
        }
    }
    var hipObj = db.catalog.findOne({name:hipnum});
    var saoObj = db.catalog.findOne({name:saonum});

    hipObj.objId = myObjId;
    db.catalog.save(hipObj);

    saoObj.objId = myObjId;
    saoObj.bii = getPreferredValue(hipObj.bii, saoObj.bii);
    saoObj.lii = getPreferredValue(hipObj.lii, saoObj.lii);
    saoObj.radeg = getPreferredValue(hipObj.radeg, saoObj.redeg);
    saoObj.decdeg = getPreferredValue(hipObj.decdeg, saoObj.decdeg);
    saoObj.rapm = getPreferredValue(hipObj.rapm, saoObj.rapm);
    saoObj.decpm = getPreferredValue(hipObj.decpm, saoObj.decpm);
    saoObj.vmag = getPreferredValue(hipObj.vmag, saoObj.vmag);
    saoObj.class = getPreferredValue(hipObj.class, saoObj.class);
    saoObj.parallax = getPreferredValue(hipObj.parallax, saoObj.parallax);

    db.catalog.save(saoObj);
});

function map() {
    emit(this.objId, {this.})
}
function getPreferredValue() {
    var length = arguments.length;
    for (var i = 0; i < length; i++) {
        if (arguments[i] != null) {
            return arguments[i];
        }
    }
    return null;
}