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
	Encode []string `json:"encode"`
  FileNames []string `json:"filenames"`
  MainFile string `json:"mainfile"`
  Flags string `json:"flags"`
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

	tb64 := t.Encode
  filenames := t.FileNames
  flags := t.Flags
  mainfile := t.MainFile
	if err != nil {
		panic(err)
	}

  for fileIndex := 0;  fileIndex < len(filenames); fileIndex++ {
    dec, err := base64.StdEncoding.DecodeString(tb64[fileIndex])

    f,err := os.Create(filenames[fileIndex])
    if err != nil {
  		panic(err)
  	}
  	defer f.Close()

    //wypisz zdekodowana zawartosc do pliku .c o ustalonej nazwie
  	if _, err := f.Write(dec); err != nil {
  		panic(err)
  	}

  	if err := f.Sync(); err != nil {
  		panic(err)
  	}
  }
  log.Println(mainfile)

  //wywolaj polecenie kompilacji wybranego pliku glownego z flagami kompilacji
  cmd := exec.Command("bash", "-c", "gcc " + mainfile + " " + flags)
	stderr, err := cmd.StderrPipe()
	if err != nil {
		log.Fatal(err)
	}

	if err := cmd.Start(); err != nil {
		log.Fatal(err)
	}

	slurp, _ := ioutil.ReadAll(stderr)
	fmt.Printf("%s\n", slurp)

  didCompile := true
	if err := cmd.Wait(); err != nil {
    fmt.Println("Errors found!")
    fmt.Println(err)
    didCompile = false
	}

  //umiesc w formacie json, zakoduj do b64 i wyslij jako response
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
