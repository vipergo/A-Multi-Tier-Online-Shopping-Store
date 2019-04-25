import pandas as pd
from matplotlib import pyplot as plt

def read_from_txt(file):
    f=open(file)
    return pd.read_table(f,header=None,index_col=False)

def average(df):
    return df.sum()[0]/df.count()[0]

lookup = []
search = []
buy = []

def get_result(file):
    count = 0
    sum = 0
    with open(file) as f:
        for line in f:
            sum+=float(line)
            count+=1
    avg = sum/count
    return (sum, count, avg)
#-----------------------------------------------------------------------------#

"""
print("1 client")
print(get_result("./oneClient/client_buy_timeLog_0.txt"));
print(get_result("./oneClient/client_search_timeLog_0.txt"));
print(get_result("./oneClient/client_lookup_timeLog_0.txt"));
"""

"""
print("3 client")
print(get_result("./threeClient/buy_sum.txt"));
print(get_result("./threeClient/search_sim.txt"));
print(get_result("./threeClient/lookup_sum.txt"));
"""
"""
print("5 client")
print(get_result("./fiveClient/buy_sum.txt"));
print(get_result("./fiveClient/search_sum.txt"));
print(get_result("./fiveClient/lookup_sum.txt"));
"""
"""
print("5 client with crash")
print(get_result("./fiveClient_withCrash/buy_sum.txt"));
print(get_result("./fiveClient_withCrash/search_sum.txt"));
print(get_result("./fiveClient_withCrash/lookup_sum.txt"));
"""


print("1 client cache overhead")
print(get_result("./cache_overhead/client_buy_timeLog_0.txt"));
#print(get_result("./cache_overhead/client_search_timeLog_0.txt"));
print(get_result("./cache_overhead/client_lookup_timeLog_0.txt"));

#-----------------------------------------------------------------------------#
