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

type b64Data struct {
	Encode string `json:"encode"`
}

type b64Output struct {
  Ok bool `json: "ok"`
  Errors string `json: "errors"`
}

func hello(w http.ResponseWriter, req *http.Request) {

    fmt.Fprintf(w, "hello\n")
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

  cmd := exec.Command("bash", "-c", "gcc " + fileName + " -Wall")
	stderr, err := cmd.StderrPipe()
	if err != nil {
    //fmt.Println("ONE")
		log.Fatal(err)
	}

	if err := cmd.Start(); err != nil {
    //fmt.Println("TWO")
		log.Fatal(err)
	}

	slurp, _ := ioutil.ReadAll(stderr)
	fmt.Printf("%s\n", slurp)

  didCompile := true
	if err := cmd.Wait(); err != nil {
    fmt.Println("Errors found!")
    fmt.Println(err)
    didCompile = false
		//log.Fatal(err)
	}

  //umiesc w jsonie, zakoduj do b64 i wyslij jako response
  sSlurp := string(slurp)
  retData := b64Output{didCompile, sSlurp}
  retJson, err := json.Marshal(retData)
  retEnc := base64.StdEncoding.EncodeToString(retJson)

	fmt.Fprintf(rw, retEnc)
}

func main() {

  http.HandleFunc("/hello", hello)
	http.HandleFunc("/b64", b64)

	log.Println("Go!")

  http.ListenAndServe(":8014", nil)
}
