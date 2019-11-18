package main

import (
   	"fmt"
	"log"
	"os"
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

/*

func headers(w http.ResponseWriter, req *http.Request) {

    for name, headers := range req.Header {
        for _, h := range headers {
            fmt.Fprintf(w, "%v: %v\n", name, h)
        }
    }
}
*/

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
    decoder := json.NewDecoder(req.Body)
    var t b64Data
    err := decoder.Decode(&t)
    if err != nil {
        panic(err)
    }
    log.Println(t)
	tb64 := t.Encode
	dec, err := base64.StdEncoding.DecodeString(tb64)
	if err != nil {
		panic(err)
	}

	f, err := os.Create("testDecodeServer.txt")
	if err != nil {
		panic(err)
	}
	defer f.Close()

	if _, err := f.Write(dec); err != nil {
		panic(err)
	}

	if err := f.Sync(); err != nil {
		panic(err)
	}

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




















