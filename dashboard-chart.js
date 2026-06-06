/**
 * Recharts Integration Module for Mess Financial Trends.
 * 
 * This module exports data preparing utilities and dynamic React templates using Recharts 
 * components to visualize Monthly Income vs Monthly Expenses trendlines.
 */

/**
 * Prepares and formats dynamic database data into standard Recharts JSON-compliant format.
 * 
 * @param {Array} payments - List of payment entities containing amount and date info.
 * @param {Array} expenses - List of expense entities containing amount and date info.
 * @returns {Array<Object>} Ready-to-render dataset for Recharts LineChart or AreaChart.
 */
function prepareTrendDataset(payments = [], expenses = []) {
    const historicalMonthsCount = 6;
    const dataset = [];

    for (let i = historicalMonthsCount - 1; i >= 0; i--) {
        const date = new Date();
        date.setMonth(date.getMonth() - i);
        
        const monthNum = date.getMonth();
        const yearNum = date.getFullYear();
        const monthLabel = date.toLocaleString('default', { month: 'short' });

        // Filter and aggregate total income (payments) for the given month/year
        const monthlyIncome = payments
            .filter(p => {
                const pDate = new Date(p.dateMillis || p.date);
                return pDate.getMonth() === monthNum && pDate.getFullYear() === yearNum;
            })
            .reduce((sum, p) => sum + (p.amount || 0), 0);

        // Filter and aggregate total expenses for the given month/year
        const monthlyExpense = expenses
            .filter(e => {
                const eDate = new Date(e.dateMillis || e.date);
                return eDate.getMonth() === monthNum && eDate.getFullYear() === yearNum;
            })
            .reduce((sum, e) => sum + (e.amount || 0), 0);

        dataset.push({
            name: monthLabel,
            income: monthlyIncome,
            expense: monthlyExpense,
            balance: monthlyIncome - monthlyExpense
        });
    }

    return dataset;
}

/**
 * Clean React Component string representation leveraging the Recharts library,
 * perfect for embedding in dashboards, reports, or progressive web views.
 */
const RechartsCodeTemplate = `
import React from 'react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend
} from 'recharts';

export default function CashFlowTrendChart({ data }) {
  return (
    <div style={{ width: '100%', height: 300, background: '#0F172A', padding: '16px', borderRadius: '12px', border: '1px solid #334155' }}>
      <h3 style={{ color: '#FFFFFF', margin: '0 0 4px 0', fontSize: '15px', fontWeight: 'bold' }}>Cash Flow Trend Analysis</h3>
      <p style={{ color: '#64748B', margin: '0 0 16px 0', fontSize: '10px' }}>Powered by Recharts Engine</p>
      <ResponsiveContainer width="100%" height={220}>
        <LineChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
          <XAxis dataKey="name" stroke="#94A3B8" fontSize={11} tickLine={false} />
          <YAxis stroke="#64748B" fontSize={10} tickLine={false} />
          <Tooltip 
            contentStyle={{ backgroundColor: '#1E293B', border: '1px solid #334155', borderRadius: '6px' }}
            labelStyle={{ color: '#FFFFFF', fontWeight: 'bold' }}
          />
          <Legend iconType="circle" wrapperStyle={{ fontSize: '11px', color: '#94A3B8' }} />
          <Line 
            type="monotone" 
            dataKey="income" 
            name="Income" 
            stroke="#10B981" 
            strokeWidth={3} 
            activeDot={{ r: 8 }} 
            dot={{ r: 4, stroke: '#0F172A', strokeWidth: 2 }}
          />
          <Line 
            type="monotone" 
            dataKey="expense" 
            name="Expenses" 
            stroke="#FB7185" 
            strokeWidth={3} 
            dot={{ r: 4, stroke: '#0F172A', strokeWidth: 2 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
`;

// Direct demonstration if executed via CLI
if (require.main === module) {
    // Generate standard mock payment and expense arrays for simulation
    const dummyPayments = [
        { amount: 15000, dateMillis: Date.now() },
        { amount: 12000, dateMillis: Date.now() - (30 * 24 * 60 * 60 * 1000) },
        { amount: 18000, dateMillis: Date.now() - (60 * 24 * 60 * 60 * 1000) },
        { amount: 14000, dateMillis: Date.now() - (90 * 24 * 60 * 60 * 1000) },
        { amount: 16000, dateMillis: Date.now() - (120 * 24 * 60 * 60 * 1000) },
        { amount: 19000, dateMillis: Date.now() - (150 * 24 * 60 * 60 * 1000) }
    ];

    const dummyExpenses = [
        { amount: 11000, dateMillis: Date.now() },
        { amount: 10500, dateMillis: Date.now() - (30 * 24 * 60 * 60 * 1000) },
        { amount: 13000, dateMillis: Date.now() - (60 * 24 * 60 * 60 * 1000) },
        { amount: 9500, dateMillis: Date.now() - (90 * 24 * 60 * 60 * 1000) },
        { amount: 12000, dateMillis: Date.now() - (120 * 24 * 60 * 60 * 1000) },
        { amount: 11500, dateMillis: Date.now() - (150 * 24 * 60 * 60 * 1000) }
    ];

    const preparedData = prepareTrendDataset(dummyPayments, dummyExpenses);
    console.log("=== RECHARTS FORMATTED FINANCIAL DATASET ===");
    console.log(JSON.stringify(preparedData, null, 2));
    console.log("\n=== COMPLETED SUCCESSFULLY! ===");
}

module.exports = {
    prepareTrendDataset,
    RechartsCodeTemplate
};
