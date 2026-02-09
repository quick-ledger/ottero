#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting QuickLedger Development Environment...${NC}"

# Create logs directory
mkdir -p /tmp/quickledger-logs

# Pre-cleanup: Kill any existing services to prevent conflicts
echo -e "${YELLOW}Cleaning up any existing services...${NC}"
lsof -ti:8080 | xargs kill -9 2>/dev/null
lsof -ti:5173 | xargs kill -9 2>/dev/null
pkill -f "vite" 2>/dev/null
pkill -f "pinggy" 2>/dev/null
pkill -f "pro.pinggy.io" 2>/dev/null
sleep 1

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}Shutting down all services...${NC}"
    
    # Kill all background jobs and their child processes
    jobs -p | xargs -I {} pkill -TERM -P {} 2>/dev/null
    kill $(jobs -p) 2>/dev/null
    
    # Also kill any lingering processes on port 8080 (backend)
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    
    # Kill any lingering Vite processes (frontend) and port 5173
    lsof -ti:5173 | xargs kill -9 2>/dev/null
    pkill -f "vite" 2>/dev/null
    
    # Kill any lingering Pinggy SSH tunnels
    pkill -f "pinggy" 2>/dev/null
    pkill -f "pro.pinggy.io" 2>/dev/null
    
    # Clean up log files
    rm -f /tmp/quickledger-logs/*.log
    
    echo -e "${GREEN}All services stopped.${NC}"
    exit
}

trap cleanup SIGINT SIGTERM

# Start Backend
echo -e "${GREEN}[1/3] Starting Backend...${NC}"
cd /Users/ghafarir/dev/ottero/quickledger-service
./run_local.sh > /tmp/quickledger-logs/backend.log 2>&1 &
BACKEND_PID=$!

# Start Frontend
echo -e "${GREEN}[2/3] Starting Frontend...${NC}"
cd /Users/ghafarir/dev/ottero/quickledger-react
./run_local.sh > /tmp/quickledger-logs/frontend.log 2>&1 &
FRONTEND_PID=$!

# Start Pinggy Tunnel
echo -e "${GREEN}[3/3] Starting Pinggy Tunnel...${NC}"
cd /Users/ghafarir/dev/ottero/quickledger-service
./infra/local/pinggy_tunnel_reza.sh > /tmp/quickledger-logs/pinggy.log 2>&1 &
PINGGY_PID=$!

echo -e "\n${GREEN}✓ All services started!${NC}"
echo -e "  Backend PID:  ${BACKEND_PID}"
echo -e "  Frontend PID: ${FRONTEND_PID}"
echo -e "  Pinggy PID:   ${PINGGY_PID}"
echo -e "\n${CYAN}Waiting for services to initialize...${NC}\n"

# Wait a bit for log files to be created
sleep 3

# Tail all logs with colored prefixes
echo -e "${GREEN}=== Streaming logs (Press Ctrl+C to stop all services) ===${NC}\n"

tail -f /tmp/quickledger-logs/backend.log 2>/dev/null | sed "s/^/$(echo -e ${BLUE})[BACKEND]$(echo -e ${NC}) /" &
tail -f /tmp/quickledger-logs/frontend.log 2>/dev/null | sed "s/^/$(echo -e ${MAGENTA})[FRONTEND]$(echo -e ${NC}) /" &
tail -f /tmp/quickledger-logs/pinggy.log 2>/dev/null | sed "s/^/$(echo -e ${CYAN})[PINGGY]$(echo -e ${NC}) /" &

# Wait for all background processes
wait
