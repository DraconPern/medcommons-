import java.text.SimpleDateFormat

def lines = []
new File("svn.log").eachLine { ln -> 
    lines << ln
}

def fmt = new SimpleDateFormat("yyyy-MM-dd")
def threshold = fmt.parse("2009-10-2")

def out = []
boolean skip = false
for(int i=0; i<lines.size()-2;++i) {
    if(lines[i+3] =~ /Auto update of revision tag/) {
        i+=3
        continue
    }
    
    if(lines[i] =~ /^r[0-9]{4,5}/) {
        def dt = fmt.parse(lines[i].split(/\|/)[2])
        if(dt > threshold)
            skip = false
        else
            skip = true
        continue
    }        
    
    if(lines[i].size() == 0)
        continue
    
    if(!skip)
       out << lines[i]
}

println out.join("\n")

