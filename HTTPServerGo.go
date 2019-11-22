package main

import (
   	"fmt"
	"log"
	"os"
  "io/ioutil"
	"os/exec"
	"encoding/json"
	"encoding/base64"
   	"net/http"
)

type Data struct {
    Title   string `json:"title"`
    Name   string `json:"name"`
    Numbers    Nrs    `json:"numbers"`
}

type Nrs struct {
    Age int `json:"age"`
    Foot  int `json:"foot"`
}

type b64Data struct {
	Encode string `json:"encode"`
}



func hello(w http.ResponseWriter, req *http.Request) {

    fmt.Fprintf(w, "hello\n")
}

func test(rw http.ResponseWriter, req *http.Request) {
    decoder := json.NewDecoder(req.Body)
    var t Data
    err := decoder.Decode(&t)
    if err != nil {
        panic(err)
    }
    log.Println(t)
	fmt.Fprintf(rw, "Small_success!")
}

func b64(rw http.ResponseWriter, req *http.Request) {
	//zdekoduj plik w b64 bedacy cialem post requesta
    decoder := json.NewDecoder(req.Body)
    var t b64Data
    err := decoder.Decode(&t)
    if err != nil {
        panic(err)
    }
    //wypisuje zakodowana tresc jsona
    //log.Println(t)
	tb64 := t.Encode
	dec, err := base64.StdEncoding.DecodeString(tb64)
	if err != nil {
		panic(err)
	}

	var fileName = "checkedFile.c"

	f, err := os.Create(fileName)
	if err != nil {
    fmt.Println("One")
		panic(err)
	}
	defer f.Close()

	//wypisz zdekodowana zawartosc do pliku .c o ustalonej nazwie
	if _, err := f.Write(dec); err != nil {
    fmt.Println("Two")
		panic(err)
	}

	if err := f.Sync(); err != nil {
    fmt.Println("Three")
		panic(err)
	}

  //var outputFile = "output2.txt"
	//out, err := exec.Command("gcc", fileName, " &> " + outputFile).Output()
  cmd := exec.Command("bash", "-c", "gcc " + fileName)
	stderr, err := cmd.StderrPipe()
	if err != nil {
		log.Fatal(err)
	}

	if err := cmd.Start(); err != nil {
		log.Fatal(err)
	}

	slurp, _ := ioutil.ReadAll(stderr)
	fmt.Printf("%s\n", slurp)

	if err := cmd.Wait(); err != nil {
		log.Fatal(err)
	}
    // as the out variable defined above is of type []byte we need to convert
    // this to a string or else we will see garbage printed out in our console
    // this is how we convert it to a string
    // fmt.Println(" Command Successfully Executed")
    // output := string(out[:])
    // fmt.Println(output)
	//out, err := exec.Command("gcc", fileName, "-o", outputFile).Output()
	//errRun := cmd.Run()
	// if errRun != nil {
  //   fmt.Println("Four")
	// 	panic(errRun)
	// }

	fmt.Fprintf(rw, "Small_success!")
}

/*
func decodeJson(w http.ResponseWriter, r *http.Request) {
		var u User
		if r.Body == nil {
			http.Error(w, "Please send a request body", 400)
			return
		}
		err := json.NewDecoder(r.Body).Decode(&u)
		if err != nil {
			http.Error(w, err.Error(), 400)
			return
		}
		fmt.Println(u.Id)
}
*/

func main() {

    http.HandleFunc("/hello", hello)
    //http.HandleFunc("/headers", headers)
	http.HandleFunc("/test", test)
	http.HandleFunc("/b64", b64)
	//http.HandleFunc("/", decodeJson)

	log.Println("Go!")

    http.ListenAndServe(":8014", nil)
}
