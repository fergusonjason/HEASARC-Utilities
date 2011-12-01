db.catalog.find().forEach(function(obj){
    rah = obj.rah * 15;
    ramin = obj.ramin/4;
    rasec = obj.rasec/240;

    radeg = rah + ramin + rasec;

    decd = obj.decdeg;
    decmin = obj.decmin/60;
    decsec = obj.decsec/3600;
    decdeg = decd + decmin + decsec;

    print(radeg);
    print(decdeg);
});