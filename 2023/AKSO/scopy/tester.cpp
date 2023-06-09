#include<bits/stdc++.h>
#include<errno.h>
#include<stdio.h>
#include<stddef.h>
#include<stdlib.h>
#include<string.h>
#include<unistd.h>
#include<sys/user.h>
#include<sys/wait.h>
#include<syscall.h>
#include<sys/ptrace.h>
#include<assert.h>

#define debug_enabled true
#define dbg if(debug_enabled) cerr

using namespace std;

typedef struct user_regs_struct regs;

class Test {
    private:
    const char *path = "./scopy.elf";
    virtual void _read(regs &r) = 0;
    virtual void _write(regs &r) = 0;
    virtual void _open(regs &r) = 0;
    virtual void _close(regs &r) = 0;

    protected:
    
    void run_traced() {
        pid = fork();
        assert(pid != -1);
        if(pid == 0) {
            ptrace(PTRACE_TRACEME, 0, 0, 0);
            execvp(path, argv);
            assert(0);
        }
        waitpid(pid, 0, 0);
        ptrace(PTRACE_SETOPTIONS, pid, 0, PTRACE_O_EXITKILL);
        
        for(;;) {
            ptrace(PTRACE_SYSCALL, pid, 0, 0);
            waitpid(pid, 0, 0);
            
            regs rg;
            
            ptrace(PTRACE_GETREGS, pid, 0, &rg);

            unsigned long rax = rg.orig_rax;
    
            dbg << "SYSCALL " << rax <<  " " << rg.rdi << " " << rg.rsi << " " << rg.rdx << endl;
    
            if(rax == SYS_read|| rax == SYS_write || rax == SYS_open || rax == SYS_close) {
                
                rg.rax = -1;
                ptrace(PTRACE_SYSCALL, pid, 0, 0);
                waitpid(pid, 0, 0);
                if(rax == SYS_read)
                    _read(rg);
                else if(rax == SYS_write)
                    _write(rg);
                else if(rax == SYS_open)
                    _open(rg);
                else if(rax == SYS_close)
                    _close(rg);
                else
                    assert(false);
                //Zgodnie z dokumentacją, syscall może zrobić cokolwiek z rcx oraz r11.
                rg.rcx = 0x63a9641a58d4f98f;
                rg.r11 = 0xc691a5c8d2e198a9;
                ptrace(PTRACE_SETREGS, pid, 0, &rg);
            }
            else {
                if(rax == SYS_exit) {
                    exit_code = int(rg.rdi & 255);
                }
                ptrace(PTRACE_SYSCALL, pid, 0, 0);
                waitpid(pid, 0, 0);
                if(rax == SYS_exit)
                    break;
            }
        }
    }
    
    char **argv;
    int exit_code;
    pid_t pid = 0;

    void fail(string s) {
        kill(pid, SIGKILL);
        throw s;
    }

    public:
    void run() {
        run_traced();
    }
    Test(char **_argv) : argv(_argv) {}
};

class ZBufora : public Test {
    protected:
    unsigned long fdin = 4294967295u, fdout = 4294967295u, lastfd = 2;
    size_t readlim = 4294967295u, writelim = 4294967295u;
    unordered_map<unsigned long, int> isopen;
    queue<char> input, output;

    void _open(regs &r) {
        if(pre_open(r))
            return;
        string n2 = "";
        unsigned long ptr = r.rdi;
        while(unsigned long c = (unsigned long)ptrace(PTRACE_PEEKDATA, pid, ptr, 0)) {
            vector<char> v;
            for(int i = 0; i < 8; i++) {
                v.emplace_back(char(c & 255));
                c >>= 8;
            }
            bool fail = 0;
            for(auto i : v) {
                if(i)
                    n2 += i;
                else {
                    fail = 1;
                    break;
                }
            }
            if(fail)
                break;
            ptr += 8;
            break;
        }
        string istrn = argv[1], ostrn = argv[2];
        if(n2 == istrn) {
            if(fdin != 4294967295u)
                fail("Otwarcie pliku wejsciowego dwa razy");
            fdin = ++lastfd;
            isopen[fdin] = 1;
            r.rax = fdin;
        }
        else if(n2 == ostrn) {
            if(fdout != 4294967295u)
                fail("Otwarcie pliku wyjściowego dwa razy");
            fdout = ++lastfd;
            isopen[fdout] = 1;
            r.rax = fdout;
        }
        else
            fail("Otwarcie niepotrzebnego pliku");
    }

    void _close(regs &r) {
        //Zamknięcie jest wyjątkowe, i powinno wykonać się przed pre_close.
        //Chcemy zaliczyć samą próbę zamknięcia.
        if(!isopen[r.rdi])
            fail("Zamkniecie nieotwartego pliku");
        isopen[r.rdi] = 0;
        r.rax = 0;
        if(pre_close(r))
            return;
    }

    void _read(regs &r) {
        if(pre_read(r))
            return;
        if(r.rdi != fdin)
            fail("Odczyt z pliku innego niz wejsciowego");
        if(!isopen[fdin])
            fail("Odczyt z zamknietego pliku");
        unsigned long ptr = r.rsi;
        unsigned long optr = ptr;
        vector<char> buffer;
        size_t siz = min({(size_t)r.rdx, (size_t)input.size(), readlim});
        while(buffer.size() < siz) {
            unsigned long c = (unsigned long)ptrace(PTRACE_PEEKDATA, pid, ptr, 0);
            for(int i = 0; i < 8; i++) {
                buffer.emplace_back(char(c & 255));
                c >>= 8;
            }
            ptr += 8;
        }
        for(size_t i = 0; i < siz; i++) {
            buffer[i] = input.front();
            input.pop();
        }
        for(size_t i = 0; i < buffer.size(); i += 8) {
            unsigned long c = 0;
            for(size_t j = 0; j < 8; j++)
                c |= ((unsigned long) ((unsigned char)buffer[i + j])) << (j * 8);
                //Ten cast na unsigned chara jest BARDZO ważny, bo inaczej dzieją się
                //niefajne rzeczy z bitem znaku bo char w cpp jest domyślnie ze znakiem.
            int rt = (int)ptrace(PTRACE_POKEDATA, pid, optr + i, c);
        }
        dbg << "rdsiz:" << siz << endl;
        r.rax = siz;
    }

    void _write(regs &r) {
        if(pre_write(r))
            return;
        if(r.rdi != fdout)
            fail("Zapis do pliku innego niz wyjsciowy");
        if(!isopen[fdout])
            fail("Zapis do zamknietego pliku");
        unsigned long ptr = r.rsi;
        size_t siz = min(writelim, (size_t)r.rdx);
        vector<char> buffer;
        while(buffer.size() < siz) {
            unsigned long c = (unsigned long)ptrace(PTRACE_PEEKDATA, pid, ptr, 0);
            for(int i = 0; i < 8; i++) {
                buffer.emplace_back(char(c & 255));
                c >>= 8;
            }
            ptr += 8;
        }
        for(size_t i = 0; i < siz; i++)
            output.push(buffer[i]);
        dbg << "wrsiz:" << siz << endl;
        r.rax = siz;
    }

    queue<char> brute(queue<char> data) {
        queue<char> ret;
        unsigned long long ctr = 0;
        auto freeb = [&] () {
            if(ctr) {
                ret.push(char(ctr & 255));
                ret.push(char((ctr >> 8) & 255));
            }
            ctr = 0;
        };
        while(!data.empty()) {
            char c = data.front();
            data.pop();
            if(c == 's' || c == 'S') {
                freeb();
                ret.push(c);
            }
            else
                ++ctr;
        }
        freeb();
        return ret;
    }

    virtual bool pre_open(regs &r) {return false;}
    virtual bool pre_close(regs &r) {return false;}
    virtual bool pre_read(regs &r) {return false;}
    virtual bool pre_write(regs &r) {return false;}
    

    public:
    ZBufora(char **_argv, queue<char> _input) : Test(_argv), input(_input) {
        isopen[0] = isopen[1] = isopen[2] = 1;  // stdin, stdout, stderr
    }

    void setrlim(size_t x) {
        readlim = x;
    }

    void setwlim(size_t x) {
        writelim = x;
    }

    virtual int run() {
        queue<char> correct = brute(input);
        try {
            run_traced();
        }
        catch (string e) {
            cout << e << endl;
            return -1;
        }
        for(auto [key, val] : isopen) {
            if(key != 0 && key != 1 && key != 2 && val) {
                cout << "Nie zamknieto pliku o fd " << key << endl;
                return -1;
            }
        }
        if(exit_code != 0)
            return exit_code;
        if(correct.size() != output.size()) {
            cout << "Zle wyjscie" << endl;
            dbg << "correct.size() = " << correct.size() << endl;
            dbg << "output.size() = " << output.size() << endl;
            return -1;
        }
        while(!correct.empty() && !output.empty()) {
            if(correct.front() != output.front()) {
                cout << "Zle wyjscie" << endl;
                dbg << "correct.front() = " << int(correct.front()) << endl;
                dbg << "output.front()  = " << int(output.front()) << endl;
                return -1;
            }
            correct.pop();
            output.pop();
        }
        return exit_code;
    }
};

void poprawnosciowy1() {
    queue<char> data;
    for(int i = 0; i < 5'000; i++) {
        for(int j = 0; j < i; j++)
            data.push(0);
        data.push('s');
    }
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0}; 
    ZBufora test = ZBufora(argv, data);
    int rc = test.run();
    assert(!rc);
}

void poprawnosciowy2() {
    queue<char> data;
    for(int i = 0; i < (1<<16); i++)
        data.push(char(i & 15));
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    ZBufora test = ZBufora(argv, data);
    int rc = test.run();
    assert(!rc);
}

void poprawnosciowy3() {
    queue<char> data;
    for(int i = 0; i < (1<<16); i++)
        data.push(char(i & 255));
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    ZBufora test = ZBufora(argv, data);
    int rc = test.run();
    assert(!rc);
}

void poprawnosciowy4() {
    queue<char> data;
    for(int i = 0; i < 2'000'001; i++)
        data.push((i & 1 ? 'S' : 0));
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    ZBufora test = ZBufora(argv, data);
    int rc = test.run();
    assert(!rc);        
}

void poprawnosciowy5() {
    queue<char> data;
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    ZBufora test = ZBufora(argv, data);
    int rc = test.run();
    assert(!rc);
}

void poprawnosciowy6() {
    queue<char> data;
    for(int i = 0; i < 256; i++) {
        for(int j = 0; j < 256; j++) {
            for(int k = 0; k < 256; k++) {
                data.push(char(i));
                data.push(char(j));
                data.push(char(k));
            }
        }
    }
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    ZBufora test = ZBufora(argv, data);
    int rc = test.run();
    assert(!rc);
}

void poprawnosciowy7() {
    queue<char> data;
    unsigned seed = 0x9a3b62fb;
    for(int i = 0; i < 1'234'567; i++) {
      data.push(char(seed & 255));
      seed ^= seed << 20;
      seed += 0x266ab8df;
      seed ^= seed >> 13;
    }
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    ZBufora test = ZBufora(argv, data);
    int rc = test.run();
    assert(!rc);
}

class FailParametrized : public ZBufora {
    private:
    unsigned read_cnt = 0, write_cnt = 0, open_cnt = 0, close_cnt = 0;
    unsigned read_lim, write_lim, open_lim, close_lim;
    bool close_mode;
    bool pre_read(regs &r) {
        ++read_cnt;
        if(read_cnt < read_lim)
            return false;
        r.rax = -1;
        return true;
    }
    bool pre_write(regs &r) {
        ++write_cnt;
        if(write_cnt < write_lim)
            return false;
        r.rax = -1;
        return true;
    }
    bool pre_open(regs &r) {
        ++open_cnt;
        if(open_cnt < open_lim)
            return false;
        r.rax = -1;
        return true;
    }
    bool pre_close(regs &r) {
        ++close_cnt;
        if((close_mode == false && close_cnt < close_lim) || (close_mode == true && close_cnt >= close_lim)) {
            return false;
        }
        r.rax = -1;
        return true;
    }
    public:
    int run() {
        try {
            run_traced();
        }
        catch (string e) {
            cout << e << endl;
            return -1;
        }
        for(auto [key, val] : isopen)
            if(key != 0 && key != 1 && key != 2 && val)
                    return -1;
        return exit_code;
    }
    FailParametrized(char **_argv, queue<char> _input, unsigned _open_lim, unsigned _close_lim, unsigned _read_lim, unsigned _write_lim, bool _close_mode = false) :
        ZBufora(_argv, _input), open_lim(_open_lim), close_lim(_close_lim), read_lim(_read_lim), write_lim(_write_lim), close_mode(_close_mode) {};
};

const int uinf = 4294967295u;

void odmowaotwarcia(unsigned x) {
    queue<char> data;
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    FailParametrized test = FailParametrized(argv, data, x, uinf, uinf, uinf);
    int rc = test.run();
    assert(rc == 1);  
}

void odmowaodczytu(unsigned x, unsigned y) {
    queue<char> data;
    for(unsigned i = 0; i < y; i++)
        data.push(char(i & 255));
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    FailParametrized test = FailParametrized(argv, data, uinf, uinf, x, uinf);
    int rc = test.run();
    assert(rc == 1);  
}

void odmowazapisu(unsigned x, unsigned y) {
    queue<char> data;
    for(unsigned i = 0; i < y; i++)
        data.push((i & 1) ? 's' : char(i & 255));
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    FailParametrized test = FailParametrized(argv, data, uinf, uinf, uinf, x);
    int rc = test.run();
    assert(rc == 1);  
}

void odmowazamkniecia(unsigned x, unsigned y) {
    queue<char> data;
    for(unsigned i = 0; i < y; i++)
        data.push((i & 1) ? 's' : char(i & 255));
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    FailParametrized test = FailParametrized(argv, data, uinf, x, uinf, uinf);
    int rc = test.run();
    assert(rc == 1);  
}

void odmowaotwarciaizamkniecia(unsigned x1, unsigned x2, unsigned y) {
    queue<char> data;
    for(unsigned i = 0; i < y; i++)
        data.push((i & 1) ? 's' : char(i & 255));
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    FailParametrized test = FailParametrized(argv, data, x1, x2, uinf, uinf);
    int rc = test.run();
    assert(rc == 1);  
}

void odmowadrugiegozamkniecia() {
    queue<char> data;
    for(unsigned i = 0; i < 1'000'007; i++)
        data.push((i & 1) ? 's' : char(i & 255));
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    FailParametrized test = FailParametrized(argv, data, uinf, 2, uinf, uinf, true);
    int rc = test.run();
    assert(rc == 1);  
}

void limited(unsigned x, unsigned y) {
    queue<char> data;
    for(unsigned i = 0; i < 1'000'007; i++)
        data.push((i & 1) ? 's' : char(i & 255));
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    ZBufora test = ZBufora(argv, data);
    int rc = test.run();
    assert(rc == 1);  
}

void faultyio(unsigned x1, unsigned x2, unsigned z) {
    dbg << "faultyio " << x1 << " " << x2 << " " << z << "\n";
    queue<char> data;
    unsigned seed = 0x303b62fb;
    for(int i = 0; i < z; i++) {
      data.push(char(seed & 255));
      seed ^= seed << 21;
      seed += 0x266ab89f;
      seed ^= seed >> 11;
    }
    char *argv[] = {(char*) "./scopy", (char*) "in", (char *) "out", 0};
    ZBufora test = ZBufora(argv, data);
    test.setrlim(x1);
    test.setwlim(x2);
    int rc = test.run();
    assert(!rc);
}

void poprawnosciowe() {
    poprawnosciowy1();
    poprawnosciowy2();
    poprawnosciowy3();
    poprawnosciowy4();
    poprawnosciowy5();
    poprawnosciowy6();
    poprawnosciowy7();
}

void odmowy() {
    odmowaotwarcia(1);
    odmowaotwarcia(2);
    
    //Zakładam, że bufor jest nie większy niż 500'000.
    odmowaodczytu(1, 1);
    odmowaodczytu(1, 1'000);
    odmowaodczytu(2, 1'000'000);
    odmowaodczytu(5, 3'000'000);

    odmowazapisu(1, 1);
    odmowazapisu(1, 1'000);
    odmowazapisu(2, 1'000'000);
    odmowazapisu(5, 3'000'000);
    
    odmowazamkniecia(1, 123'456);
    odmowazamkniecia(2, 654'321);

    odmowaotwarciaizamkniecia(1, 1, 1'234);
    odmowaotwarciaizamkniecia(2, 1, 777'777);
    odmowaotwarciaizamkniecia(1, 2, 696'969);
    odmowaotwarciaizamkniecia(2, 2, 731'296);

    odmowadrugiegozamkniecia();
}

void niecalkowityodczyt() {
    faultyio(1, 1, 123'456);
    faultyio(10, 17, 654'321);
    faultyio(100, 101, 111'111);
    faultyio(33, 1, 3'333);
    faultyio(1, 59, 12'345);
    faultyio(4, 4, 111'111);
    faultyio(69, 70, 1'171'111);
}

int main() {
    //poprawnosciowe();
    //odmowy();
    niecalkowityodczyt();
}
